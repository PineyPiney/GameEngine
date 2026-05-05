package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.Movement3D
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.rendering.meshes.IndexedMeshBuilder
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.rendering.meshes.vulkan.VulkanIndexedMesh
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.models.VulkanModelMesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.vulkan.pipeline.VulkanGraphicsPipeline
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanSwapchainImage
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import com.pineypiney.game_engine.util.extension_functions.put
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.detail.GLM_DEPTH_CLIP_SPACE
import glm_.detail.GlmDepthClipSpace
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.system.MemoryStack
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

	val surface = VkUtil.createSurface(vulkan.instance, window)
	val colourFormatSpace = vulkan.gpu.getSurfaceColour(surface)
	var swapchain = VkUtil.createSwapchain(vulkan.device, surface, null, window.width, window.height, colourFormatSpace.first, colourFormatSpace.second)

	// The image that is drawn to each frame, it is then blitted onto the swapchain's current image
	lateinit var drawImage: VulkanImage2D
	lateinit var depthImage: VulkanImage2D

	var frameIndex = 0
	val frameObjects = Array(swapchain.images.size) { VulkanFrameObjects(vulkan.device) }

	val computePipeline = ShaderLoader.generateComputePipelineVulkan(vulkan, ShaderLoader.INSTANCE.shaderModules[ResourceKey("compute/mouse_pos_vulkan")]!!, 8)

	init {
		updateFrameImages()
	}

	val graphicsPipelineBuilder = VulkanGraphicsPipeline.Builder()
	val trianglePipeline = graphicsPipelineBuilder
		.shaders("vulkan/triangle", "vulkan/colour")
		.generateLayout(vulkan.device)
		.inputTopology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
		.polygonMode(VK10.VK_POLYGON_MODE_FILL)
		.cullMode(VK10.VK_CULL_MODE_NONE, VK10.VK_FRONT_FACE_CLOCKWISE)
		.disableMultisampling()
		.disableBlending()
		.disableDepthTest()
		.colourFormat(drawImage.format.vulkan)
		.depthFormat(VK10.VK_FORMAT_UNDEFINED)
		.build(vulkan.device)

	val meshPipeline = graphicsPipelineBuilder.clear()
		.shaders("vulkan/2D", "vulkan/texture")
		.generateLayout(vulkan.device)
		.inputTopology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
		.polygonMode(VK10.VK_POLYGON_MODE_FILL)
		.cullMode(VK10.VK_CULL_MODE_NONE, VK10.VK_FRONT_FACE_CLOCKWISE)
		.disableMultisampling()
		.disableBlending()
		.enableDepthTest(true, VK10.VK_COMPARE_OP_GREATER_OR_EQUAL)
		.colourFormat(drawImage.format.vulkan)
		.depthFormat(depthImage.format.vulkan)
		.build(vulkan.device)

	val mesh: VulkanIndexedMesh = ModelLoader[ResourceKey("gltf/Arrow")].meshes.first() as VulkanModelMesh
	val quadMesh: VulkanIndexedMesh

	val brokeTexture = TextureLoader[ResourceKey("broke")] as VulkanImage2D
	val missingTexture = Texture2D.missing as VulkanImage2D

	init {
		vulkan.deletionQueue.pushAll(trianglePipeline, meshPipeline)

		val meshBuilder = IndexedMeshBuilder(VertexAttribute.POSITION, VertexAttribute.TEX_U, VertexAttribute.NORMAL, VertexAttribute.TEX_V, VertexAttribute.COLOUR)
		meshBuilder.startQuad()
			.vertex(.5f, -.5f, 0f).texture(1f, 0f)
			.vertex(.5f, .5f, 0f).texture(1f, 1f)
			.vertex(-.5f, .5f, 0f).texture(0f, 1f)
			.vertex(-.5f, -.5f, 0f).texture(0f, 0f)

		quadMesh = meshBuilder.build() as VulkanIndexedMesh
	}

	override fun init() {
		camera.range = Vec2(1000f, 0.1f)
		camera.init()
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
		frameObjects.deletionQueue.flush()
		frameObjects.frameDescriptorAllocator.clearPools()

		frameObjects.swapchainSemaphore.recreate()
		frameObjects.renderSemaphore.recreate()

		// Wait until the fence is ready, it will be signalled by the previous render cycle
		frameObjects.renderFence.wait(1000000000L)

		// Get the next swapchain image to draw to, and signal the swapchain semaphore once fetched
		val swapchainImage = swapchain.acquireNextImage(1000000000, frameObjects.swapchainSemaphore, null)
		if (swapchainImage == null) {
			updateSwapchain(window.size)
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
		drawImage.transition(commandBuffer, VK10.VK_IMAGE_LAYOUT_GENERAL, false)
		val api = getRenderingApi()

		// Execute Compute Shader
		computePipeline.bind(api)
		commandBuffer.bindPipeline(computePipeline)
		api.updateUniforms(computePipeline)
		val mousePos = window.input.mouse.lastPos.pixels
		computePipeline.getBuffer("mousePos")?.put(mousePos)

		computePipeline.updatePushConstants(frameObjects[frameIndex])
		commandBuffer.dispatch(Math.ceilDiv(swapchainImage.size.x, 16), Math.ceilDiv(swapchainImage.size.y, 16))

		// Execute Graphics Shader
		drawImage.transition(commandBuffer, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
		depthImage.transition(commandBuffer, VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL)
		renderGeometry(commandBuffer)


		// Copy the Draw Image to the Swapchain Image
		drawImage.transition(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		swapchainImage.transition(commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, false)
		drawImage.copyTo(commandBuffer, swapchainImage)

		// Prepare the Swapchain Image for presentation
		swapchainImage.transition(commandBuffer, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
	}

	fun renderGeometry(cmd: PoolAndBuffer) {

		MemoryStack.stackPush().use { stack ->

			val colourAttachments = VkStructs.createAttachmentInfos(stack, drawImage, null)

			val depthClearValue = VkStructs.clearDepthStencil(stack, 0f, 0)
			val clearValue = VkClearValue.calloc().depthStencil(depthClearValue)
			val depthAttachment = VkStructs.createAttachmentInfo(stack, depthImage, clearValue)

			val renderInfo = VkStructs.createRenderingInfo(stack, glm.min(window.size, drawImage.size), colourAttachments, depthAttachment)
			cmd.beginRendering(renderInfo)

			val api = getRenderingApi()
			val frameObjects = frameObjects[frameIndex]

			val viewport = getViewport()
			api.setViewport(viewport)
			api.setScissors(viewport)

			// Draw Triangle
			trianglePipeline.bind(api)
//		api.draw(3, 0)


			// Draw Model Mesh
			meshPipeline.bind(api)

			meshPipeline.setImage("ourTexture", brokeTexture)
			api.updateUniforms(meshPipeline)

			meshPipeline.getBuffer("model")?.put(projection * view)
			meshPipeline.getBuffer("vertexBuffer")?.putLong(mesh.vertexBufferAddress)
			meshPipeline.updatePushConstants(frameObjects)

			mesh.bindAndDraw(api)


			// Draw Quad
			meshPipeline.setImage("ourTexture", missingTexture)
			api.updateUniforms(meshPipeline)

			meshPipeline.getBuffer("model")?.put(projection * view * Mat4(1f).translate(Vec3(2f, 0f, 0f)))
			meshPipeline.getBuffer("vertexBuffer")?.putLong(quadMesh.vertexBufferAddress)
			meshPipeline.updatePushConstants(frameObjects)

			quadMesh.bindAndDraw(api)

			cmd.endRendering()
		}
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
		MemoryStack.stackPush().use { stack ->
			val frameObjects = frameObjects[frameIndex]
			val cmdInfo = VkStructs.createBufferSubmits(stack, cmd.buffer, 0)
			// Wait for the swapchain semaphore
			val waitInfo = VkStructs.createSemaphoreSubmits(stack, frameObjects.swapchainSemaphore, KHRSynchronization2.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT_KHR, 0, 1L)
			// Signal the render semaphore
			val signalInfo = VkStructs.createSemaphoreSubmits(stack, frameObjects.renderSemaphore, VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT, 0, 1L)
			val submitInfo = VkStructs.createSubmitInfo2s(stack, cmdInfo, signalInfo, waitInfo)
			VK13.vkQueueSubmit2(vulkan.queue, submitInfo, frameObjects.renderFence.handle)
		}
	}

	fun present() {
		MemoryStack.stackPush().use { stack ->
			// Wait for the render semaphore, and then present the swapchain to the screen
			val presentInfo = VkStructs.createPresentInfo(stack, swapchain, frameObjects[frameIndex].renderSemaphore)
			val err = KHRSwapchain.vkQueuePresentKHR(vulkan.queue, presentInfo)
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || err == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
				updateSwapchain(window.size)
			} else VkUtil.processError(err, "Failed to present swapchain image to screen")
		}
	}

	fun updateSwapchain(size: Vec2i) {
		vulkan.device.waitIdle()
		swapchain = VkUtil.createSwapchain(vulkan.device, surface, swapchain, size.x, size.y, colourFormatSpace.first, colourFormatSpace.second)
	}

	fun updateFrameImages() {
		val usage = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or
				VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT or
				VK10.VK_IMAGE_USAGE_STORAGE_BIT or
				VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT

		drawImage = VkUtil.createImage(vulkan.device, "Draw Image", VK10.VK_IMAGE_TYPE_2D, TextureFormat.RGBA16F, usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT, Vec2i(window.size))
		depthImage = VkUtil.createImage(
			vulkan.device,
			"Depth Image",
			VK10.VK_IMAGE_TYPE_2D,
			TextureFormat.DEPTH32F,
			VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
			VK10.VK_IMAGE_ASPECT_DEPTH_BIT,
			Vec2i(window.size)
		)

		computePipeline.setImage("image", drawImage, VK10.VK_IMAGE_LAYOUT_GENERAL)
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

		drawImage.delete()
		depthImage.delete()
		updateFrameImages()
	}

	override fun delete() {
		frameObjects.deleteArray()
		drawImage.delete()
		depthImage.delete()
		swapchain.delete()
		surface.delete()
	}
}