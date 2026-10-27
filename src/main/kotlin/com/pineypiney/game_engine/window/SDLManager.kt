package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.util.extension_functions.orOfLong
import kool.map
import org.lwjgl.opengl.GL
import org.lwjgl.sdl.SDLInit
import org.lwjgl.sdl.SDLVideo
import org.lwjgl.sdl.SDLVulkan
import org.lwjgl.system.MemoryUtil

object SDLManager : WindowApi {

	override fun init() {
		check(SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO or SDLInit.SDL_INIT_HAPTIC or SDLInit.SDL_INIT_GAMEPAD or SDLInit.SDL_INIT_EVENTS or SDLInit.SDL_INIT_SENSOR)) { "Unable to initialize SDL" }
	}

	override fun createWindow(title: String, width: Int, height: Int, properties: WindowProperties): Window {

		var flags = properties.flags.orOfLong(WindowFlags::sdl)
		when(val graphics = properties.graphicsApi) {
			is WindowProperties.Graphics.OPENGL -> {
				flags = flags or SDLVideo.SDL_WINDOW_OPENGL
				if(graphics.multisamples > 0){
					SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_MULTISAMPLEBUFFERS, 1)
					SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_MULTISAMPLESAMPLES, graphics.multisamples)
				}
			}
			else -> flags = flags or SDLVideo.SDL_WINDOW_VULKAN
		}

		if(!properties.visible) flags = flags or SDLVideo.SDL_WINDOW_HIDDEN
		if(!properties.decorated) flags = flags or SDLVideo.SDL_WINDOW_BORDERLESS
		if(properties.fullscreen) flags = flags or SDLVideo.SDL_WINDOW_FULLSCREEN
		val window = SDLWindow(title, width, height, properties.graphicsApi is WindowProperties.Graphics.OPENGL, flags).apply { vSync = properties.vSync }

		if(properties.graphicsApi is WindowProperties.Graphics.OPENGL) {
			// Make the OpenGL context current
			val ctx = SDLVideo.SDL_GL_CreateContext(window.windowHandle)
			SDLVideo.SDL_GL_MakeCurrent(window.windowHandle, ctx)

			GL.createCapabilities()
		}

		return window
	}

	override fun getVulkanExtensions(): List<String> {
		val sdlExtensions = SDLVulkan.SDL_Vulkan_GetInstanceExtensions() ?: throw RuntimeException("Missing required SDL extensions")
		return sdlExtensions.map(MemoryUtil::memUTF8)
	}

	override fun delete() {
		SDLInit.SDL_Quit()
	}
}