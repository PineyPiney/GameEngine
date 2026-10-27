package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.rendering.openxr.OpenXrFramebuffer
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.window.WindowApi
import glm_.vec3.Vec3i
import org.lwjgl.openxr.XrInstance
import org.lwjgl.openxr.XrSwapchain
import org.lwjgl.system.MemoryStack

// This interface is to manage Graphics Api-specific OpenXR functionality
interface XrGraphicsApi {

	fun getName(): String
	fun getExtensions(): List<String>
	fun getRequirements(stack: MemoryStack, instance: XrInstance, systemID: Long): Pair<Vec3i, Vec3i>
	fun getColourFormats(): List<TextureFormat>

	fun createApi(name: String, xrInstance: XrInstance, systemId: Long, windowApi: WindowApi): ApiManager
	fun createFramebuffer(stack: MemoryStack, api: ApiManager, width: Int, height: Int, colourSwapchain: XrSwapchain, depthSwapchain: XrSwapchain, colourFormat: Int, depthFormat: Int): OpenXrFramebuffer
	fun createSession(stack: MemoryStack, openxr: OpenXrManager, graphicsApi: ApiManager, window: Window): OpenXrSession
}