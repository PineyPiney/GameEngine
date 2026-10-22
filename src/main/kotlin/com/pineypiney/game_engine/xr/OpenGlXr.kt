package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.OpenGlManager
import com.pineypiney.game_engine.rendering.openxr.OpenXrFramebuffer
import com.pineypiney.game_engine.rendering.openxr.OpenXrGlFramebuffer
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.LwjglUtil
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.xr.XrUtil.getOpenGlSwapchainImages
import glm_.vec3.Vec3i
import org.lwjgl.openxr.KHROpenGLEnable
import org.lwjgl.openxr.XrGraphicsRequirementsOpenGLKHR
import org.lwjgl.openxr.XrInstance
import org.lwjgl.openxr.XrSwapchain
import org.lwjgl.system.MemoryStack

object OpenGlXr : XrGraphicsApi {


	override fun getName(): String = "OpenGL"
	override fun getExtensions(): List<String> = listOf(KHROpenGLEnable.XR_KHR_OPENGL_ENABLE_EXTENSION_NAME)
	override fun getRequirements(stack: MemoryStack, instance: XrInstance, systemID: Long): Pair<Vec3i, Vec3i> {
		val requirements = XrGraphicsRequirementsOpenGLKHR.calloc(stack).`type$Default`()
		KHROpenGLEnable.xrGetOpenGLGraphicsRequirementsKHR(instance, systemID, requirements)
		return LwjglUtil.version(requirements.minApiVersionSupported()) to LwjglUtil.version(requirements.maxApiVersionSupported())
	}

	override fun getColourFormats(): List<TextureFormat> = listOf(TextureFormat.RGB10A2, TextureFormat.RGBA16F, TextureFormat.RGBA8, TextureFormat.RGBA8S)

	override fun createApi(name: String, xrInstance: XrInstance, systemId: Long): ApiManager = OpenGlManager
	override fun createFramebuffer(
		stack: MemoryStack,
		api: ApiManager,
		width: Int,
		height: Int,
		colourSwapchain: XrSwapchain,
		depthSwapchain: XrSwapchain,
		colourFormat: Int,
		depthFormat: Int
	): OpenXrFramebuffer {

		val colourImages = getOpenGlSwapchainImages(stack, colourSwapchain)
		val depthImages = getOpenGlSwapchainImages(stack, depthSwapchain)

		return OpenXrGlFramebuffer(
			colourImages.mapIndexed { i, image ->
				OpenXrGlTexture(
					"OpenXR Swapchain colour texture $i",
					image.image(),
					colourFormat
				)
			},
			depthImages.mapIndexed { i, image ->
				OpenXrGlTexture(
					"OpenXR Swapchain depth texture $i",
					image.image(),
					depthFormat
				)
			}
		)
	}

	override fun createSession(
		stack: MemoryStack,
		openxr: OpenXrManager,
		graphicsApi: ApiManager,
		window: WindowI
	): OpenXrSession {
		return XrUtil.createOpenGlWin32Session(stack, openxr, window)
	}
}