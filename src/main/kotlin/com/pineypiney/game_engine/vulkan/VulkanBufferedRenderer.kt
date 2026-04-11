package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.Movement3D
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.meshes.IndexedMeshBuilder
import com.pineypiney.game_engine.rendering.meshes.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import com.pineypiney.game_engine.util.extension_functions.put
import com.pineypiney.game_engine.vulkan.pipeline.VulkanGraphicsPipeline
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.detail.GLM_DEPTH_CLIP_SPACE
import glm_.detail.GlmDepthClipSpace
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*

open class VulkanBufferedRenderer<G : WindowGameLogic>(override val window: WindowI, val engine: VulkanGameEngine<Logic>) : WindowRendererI<G> {

	val vulkan = engine.vulkanManager
	override val camera: PerspectiveCamera = PerspectiveCamera(window)

	override val viewPos: Vec3 = Vec3(0f)
	override val view: Mat4 = Mat4()
	override val projection: Mat4 = Mat4()
	override val guiProjection: Mat4 = Mat4()
	override var viewportSize: Vec2i = window.framebufferSize
	override var aspectRatio: Float = window.aspectRatio

	val movement = Movement3D.default(window, camera as Camera, 1f)


	val swapchain get() = vulkan.swapchain
	val drawImage get() = vulkan.drawImage
	val depthImage get() = vulkan.depthImage

	var frameIndex = 0
	val frameObjects = Array(swapchain.images.size) { VulkanFrameObjects(vulkan.device) }

	val computePipeline = ShaderLoader.generateComputePipelineVulkan(vulkan, ShaderLoader.INSTANCE.shaderModules[ResourceKey("compute/mouse_pos_vulkan")]!!, 8)

	val graphicsPipelineBuilder = VulkanGraphicsPipeline.Builder()
	val trianglePipeline = graphicsPipelineBuilder
		.setLayout(VkUtil.createPipelineLayout(vulkan.device, vulkan.pLayout))
		.shaders("vulkan/triangle", "vulkan/colour")
		.inputTopology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
		.polygonMode(VK10.VK_POLYGON_MODE_FILL)
		.cullMode(VK10.VK_CULL_MODE_NONE, VK10.VK_FRONT_FACE_CLOCKWISE)
		.disableMultisampling()
		.disableBlending()
		.disableDepthTest()
		.colourFormat(drawImage.format)
		.depthFormat(VK10.VK_FORMAT_UNDEFINED)
		.build(vulkan.device)

	val meshLayout = VkUtil.createPipelineLayout(vulkan.device, vulkan.pLayout, 72, VK10.VK_SHADER_STAGE_VERTEX_BIT)
	val meshPipeline = graphicsPipelineBuilder.clear()
		.setLayout(meshLayout)
		.shaders("vulkan/2D", "fragment/colour_primitives")
		.inputTopology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
		.polygonMode(VK10.VK_POLYGON_MODE_FILL)
		.cullMode(VK10.VK_CULL_MODE_NONE, VK10.VK_FRONT_FACE_CLOCKWISE)
		.disableMultisampling()
		.disableBlending()
		.enableDepthTest(true, VK10.VK_COMPARE_OP_GREATER_OR_EQUAL)
		.colourFormat(drawImage.format)
		.depthFormat(depthImage.format)
		.build(vulkan.device)

	val mesh: VulkanIndexedMesh

	init {
		vulkan.deletionQueue.pushAll(trianglePipeline, meshPipeline)

		val meshBuilder = IndexedMeshBuilder(VertexAttribute.POSITION, VertexAttribute.TEX_U, VertexAttribute.NORMAL, VertexAttribute.TEX_V, VertexAttribute.COLOUR)
		meshBuilder.startQuad()
			.vertex(.5f, -.5f, 0f).colour(0f, 0f, 0f, 1f)
			.vertex(.5f, .5f, 0f).colour(.5f, .5f, .5f, 1f)
			.vertex(-.5f, .5f, 0f).colour(1f, 0f, 0f, 1f)
			.vertex(-.5f, -.5f, 0f).colour(0f, 1f, 0f, 1f)

		val model = ModelLoader[ResourceKey("gltf/Arrow")]
		mesh = model.meshes.first() as VulkanModelMesh
//		mesh = meshBuilder.buildModel("Vulkan Mesh", engine.resourcesLoader.factory) as ModelMeshVulkan
	}

	override fun init() {
		camera.range = Vec2(1000f, 0.1f)
		camera.init()
		updateDescriptorSets()
	}

	override fun render(game: G, tickDelta: Double) {
		if (window.width == 0 || window.height == 0) return

		// Vulkan uses 0-1 depth
		GLM_DEPTH_CLIP_SPACE = GlmDepthClipSpace.ZERO_TO_ONE
		camera.getView(view)
		camera.getProjection(projection)
		// Vulkan's
		projection[1, 1] = projection[1, 1] * -1

		val frameObjects = frameObjects[frameIndex]

		frameObjects.swapchainSemaphore.recreate()
		frameObjects.renderSemaphore.recreate()

		// Wait until the fence is ready, it will be signalled by the previous render cycle
		frameObjects.renderFence.wait(1000000000L)

		// Get the next swapchain image to draw to, and signal the swapchain semaphore once fetched
		val swapchainImage = swapchain.acquireNextImage(1000000000, frameObjects.swapchainSemaphore, null)
		if (swapchainImage == null) {
			vulkan.updateSwapchain(window.size)
			return
		}

		frameObjects.renderFence.reset()

		val commandBuffer = frameObjects.commands
		commandBuffer.resetBuffer()
		commandBuffer.begin(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)


		renderLayer(0, game, tickDelta, null) { transformComponent.worldPosition.z }
		renderLayer(1, game, tickDelta, null) { transformComponent.worldPosition.z }

//		renderToSwapchain(commandBuffer, swapchainImage)
		renderWithFramebuffer(commandBuffer, swapchainImage)
//		val buffer = swapchainImage.getData(4)
//		buffer.free()

		commandBuffer.end()

		submit(commandBuffer)
		present()
		VK10.vkQueueWaitIdle(vulkan.queue)

		frameIndex = (frameIndex + 1) % this.frameObjects.size
	}

	fun renderWithFramebuffer(commandBuffer: PoolAndBuffer, swapchainImage: VulkanSwapchainImage) {

		// Set the Draw Image's mode to general
		drawImage.transition(commandBuffer.buffer, VK10.VK_IMAGE_LAYOUT_GENERAL, false)

		// Execute Compute Shader
		commandBuffer.bindPipeline(computePipeline)
		commandBuffer.bindDescriptorSets(vulkan, computePipeline)
		val mousePos = window.input.mouse.lastPos.pixels
		val constants = MemoryUtil.memAlloc(8).putInt(mousePos.x).putInt(window.height - mousePos.y).flip()
		commandBuffer.pushConstants(computePipeline, VK10.VK_SHADER_STAGE_COMPUTE_BIT, constants)
//		commandBuffer.dispatch(Math.ceilDiv(swapchainImage.size.x, 16), Math.ceilDiv(swapchainImage.size.y, 16))

		// Execute Graphics Shader
		drawImage.transition(commandBuffer.buffer, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
		depthImage.transition(commandBuffer.buffer, VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL)
		renderGeometry(commandBuffer)

		// Copy the Draw Image to the Swapchain Image
		drawImage.transition(commandBuffer.buffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		swapchainImage.transition(commandBuffer.buffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, false)
		drawImage.copyTo(commandBuffer.buffer, swapchainImage, drawImage.size, window.framebufferSize)

		// Prepare the Swapchain Image for presentation
		swapchainImage.transition(commandBuffer.buffer, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
	}

	fun renderGeometry(cmd: PoolAndBuffer) {

		val colourClearValue = VkStructs.clearColour(Vec4(.1f, .1f, .1f, 1f))
		val colourAttachments = VkStructs.createAttachmentInfos(drawImage, VkClearValue.calloc().color(colourClearValue))

		val depthClearValue = VkStructs.clearDepthStencil(0f, 0)
		val clearValue = VkClearValue.calloc().depthStencil(depthClearValue)
		val depthAttachment = VkStructs.createAttachmentInfo(depthImage, clearValue)

		val renderInfo = VkStructs.createRenderingInfo(glm.min(window.size, drawImage.size), colourAttachments, depthAttachment)
		cmd.beginRendering(renderInfo)

		val api = getRenderingApi()

		val viewport = getViewport()
		api.setViewport(viewport)
		api.setScissors(viewport)

		// Draw Triangle
		trianglePipeline.bind(api)
//		api.draw(3, 0)


		// Draw Rectangle
		meshPipeline.bind(api)
		val bytes = MemoryUtil.memAlloc(72)
		bytes.put(projection * view)
		bytes.putLong(mesh.vertexBufferAddress)
		cmd.pushConstants(meshPipeline, VK10.VK_SHADER_STAGE_VERTEX_BIT, bytes.flip())
		api.bindIndices(mesh.indexBuffer.buffer, 0L, VK10.VK_INDEX_TYPE_UINT32)
		api.drawIndexed(mesh.count, 0)

		cmd.endRendering()
	}

	fun renderLayer(layer: Int, game: G, tickDelta: Double, framebuffer: Framebuffer? = null) =
		renderLayer(game.gameObjects[layer], tickDelta, framebuffer?.FBO ?: 0) { -(transformComponent.worldPosition - camera.cameraPos).length2() }

	fun <C : Comparable<C>> renderLayer(layer: Int, game: G, tickDelta: Double, framebuffer: Framebuffer? = null, sort: GameObject.() -> C) =
		renderLayer(game.gameObjects[layer], tickDelta, framebuffer?.FBO ?: 0, sort)

	fun renderLayer(layer: Collection<GameObject>, tickDelta: Double, framebuffer: Int = 0) =
		renderLayer(layer, tickDelta, framebuffer) { -(transformComponent.worldPosition - camera.cameraPos).length2() }

	open fun <C : Comparable<C>> renderLayer(layer: Collection<GameObject>, tickDelta: Double, framebuffer: Int = 0, sort: GameObject.() -> C) {
		for (o in layer.flatMap { it.catchRenderingComponents() }.sortedBy(sort)) {
			renderObject(o, tickDelta, framebuffer)
		}
	}

	open fun renderObject(obj: GameObject, tickDelta: Double, framebuffer: Int = 0) {
		val renderedComponents = obj.components.filterIsInstance<RenderedComponentI>().filter { it.visible }
		if (renderedComponents.isNotEmpty()) {
			for (c in obj.components.filterIsInstance<PreRenderComponent>()) c.preRender(this, tickDelta)
			for (c in renderedComponents) c.render(this, tickDelta)
		} else for (c in obj.components.filterIsInstance<PreRenderComponent>()) {
			if (!c.whenVisible) c.preRender(this, tickDelta)
		}
	}

	fun submit(cmd: PoolAndBuffer) {
		val frameObjects = frameObjects[frameIndex]
		val cmdInfo = VkStructs.createBufferSubmits(cmd.buffer, 0)
		// Wait for the swapchain semaphore
		val waitInfo = VkStructs.createSemaphoreSubmits(frameObjects.swapchainSemaphore, KHRSynchronization2.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT_KHR, 0, 1L)
		// Signal the render semaphore
		val signalInfo = VkStructs.createSemaphoreSubmits(frameObjects.renderSemaphore, VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT, 0, 1L)
		val submitInfo = VkStructs.createSubmitInfo2s(cmdInfo, signalInfo, waitInfo)
		VK13.vkQueueSubmit2(vulkan.queue, submitInfo, frameObjects.renderFence.handle)
	}

	fun present() {
		// Wait for the render semaphore, and then present the swapchain to the screen
		val presentInfo = VkStructs.createPresentInfo(swapchain, frameObjects[frameIndex].renderSemaphore)
		val err = KHRSwapchain.vkQueuePresentKHR(vulkan.queue, presentInfo)
		if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
			vulkan.updateSwapchain(window.size)
		} else VkUtil.processError(err, "Failed to present swapchain image to screen")
	}

	fun updateDescriptorSets() {
		val imageInfo = VkDescriptorImageInfo.calloc(1)
			.imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL)
			.imageView(drawImage.imageView)

		val writeSet = VkWriteDescriptorSet.calloc(1)
			.`sType$Default`()
			.dstBinding(0)
			.dstSet(vulkan.descriptorSet)
			.descriptorCount(1)
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
			.pImageInfo(imageInfo)

		VK10.vkUpdateDescriptorSets(vulkan.device.device, writeSet, null)
	}

	override fun getRenderingApi(): RenderingApi {
		return frameObjects[frameIndex].api
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {

		camera.updateAspectRatio(window.aspectRatio)
		viewportSize = window.size
		aspectRatio = window.aspectRatio

		glm.ortho(-aspectRatio, aspectRatio, -1f, 1f, guiProjection)
		guiProjection[1, 1] = guiProjection[1, 1] * -1f
	}

	override fun delete() {
		frameObjects.deleteArray()
	}
}