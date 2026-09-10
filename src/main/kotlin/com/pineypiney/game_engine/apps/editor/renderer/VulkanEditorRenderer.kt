package com.pineypiney.game_engine.apps.editor.renderer

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.vulkan.VulkanGameRenderer
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanSwapchainImage
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK12

open class VulkanEditorRenderer(
	window: WindowI,
	vulkan: VulkanManager,
	override val settings: EditorSettings,
	override val sort: GameObject.() -> Float,
	override val depth: Boolean
) : VulkanGameRenderer<EditorScreen, OrthographicCamera>(window, vulkan, OrthographicCamera(window)), EditorRenderer {

	lateinit var sceneDrawImage: VulkanImage2D
	lateinit var sceneDepthImage: VulkanImage2D

	override var backgroundColour = Colour(0, 255, 0)
	var sceneMesh: Mesh = createSceneBufferMesh()

	override fun renderWithFramebuffer(cmd: PoolAndBuffer, game: EditorScreen, tickDelta: Double, swapchainImage: VulkanSwapchainImage) {

		// Execute Graphics Shader

		sceneDrawImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false)
		sceneDepthImage.transition(cmd, VK12.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, false)

		val sceneBox = game.getSceneBox()
		viewportSize = sceneBox.size
		aspectRatio = sceneBox.aspectRatio
		updateGui()

		MemoryStack.stackPush().use { stack ->
			// Render the scene box
			beginRendering(stack, cmd, sceneDrawImage, sceneDepthImage, Viewport(Vec2i(0), sceneBox.size), backgroundColour.rgbaVec)
			for ((_, layer) in game.sceneObjects.map) renderLayer(layer, tickDelta, sort)
			game.transformer?.let {
				for (obj in it.catchRenderingComponents()) renderObject(obj, tickDelta)
			}
			cmd.endRendering()

			// Render the Editor GUI

			drawImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false)
			depthImage.transition(cmd, VK12.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, false)
			sceneDrawImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)

			viewportSize = window.framebufferSize
			aspectRatio = window.aspectRatio
			updateGui()

			beginRendering(stack, cmd, drawImage, depthImage, clearColour = Vec4(0f))
			screenShader.setUp(screenUniforms, this)
			screenShader.setTexture("screenTexture", sceneDrawImage)
			screenShader.draw("vertexBuffer", sceneMesh, getRenderingApi())

			renderLayer(1, game, tickDelta, sort)
			cmd.endRendering()
		}


		// Copy the Draw Image to the Swapchain Image
		drawImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		swapchainImage.transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, false)

		drawImage.copyTo(cmd, swapchainImage, Vec3i(0, drawImage.height, 0), Vec3i(drawImage.width, 0, 1))

		// Prepare the Swapchain Image for presentation
		swapchainImage.transition(cmd, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
	}

	override fun updateFrameImages() {
		val drawUsage = VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT
		val depthUsage = VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT

		val sceneBox = EditorScreen.getSceneBox(settings, window)

		drawImage = VkUtil.createImage(vulkan.device, "Draw Image", vulkan.drawFormat, drawUsage or VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, window.size)
		sceneDrawImage = VkUtil.createImage(vulkan.device, "Scene Draw Image", vulkan.drawFormat, drawUsage or VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT, sceneBox.size)
		depthImage = VkUtil.createImage(vulkan.device, "Depth Image", vulkan.depthFormat, depthUsage, VK10.VK_IMAGE_ASPECT_DEPTH_BIT or VK10.VK_IMAGE_ASPECT_STENCIL_BIT, Vec2i(window.size))
		sceneDepthImage = VkUtil.createImage(vulkan.device, "Scene Depth Image", vulkan.depthFormat, depthUsage, VK10.VK_IMAGE_ASPECT_DEPTH_BIT or VK10.VK_IMAGE_ASPECT_STENCIL_BIT, sceneBox.size)
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		val sceneBox = EditorScreen.getSceneBox(settings, window)
		camera.updateAspectRatio(sceneBox.aspectRatio)

		// This is updated here for TransformComponent updates
		viewportSize = window.framebufferSize
		aspectRatio = window.aspectRatio

		drawImage.delete()
		depthImage.delete()
		sceneDrawImage.delete()
		sceneDepthImage.delete()
		updateFrameImages()

		sceneMesh.delete()
		sceneMesh = createSceneBufferMesh()
	}

	override fun delete() {
		super.delete()
		sceneDrawImage.delete()
		sceneDepthImage.delete()
		sceneMesh.delete()
	}

	companion object {
		val screenShader = ShaderLoader[ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer")]
		val screenUniforms = screenShader.compileUniforms()
	}
}