package com.pineypiney.game_engine.window

import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import kool.map
import org.lwjgl.sdl.SDLVideo
import org.lwjgl.sdl.SDL_Rect
import org.lwjgl.system.MemoryStack

class SDLDisplay(val handle: Int) : Monitor {

	override val pos: Vec2i

	init {
		MemoryStack.stackPush().use { stack ->
			val rect = SDL_Rect.calloc(stack)
			SDLVideo.SDL_GetDisplayBounds(handle, rect)
			pos = Vec2i(rect.x(), rect.y())
		}
	}

	override val contentScale: Vec2 = Vec2(SDLVideo.SDL_GetDisplayContentScale(handle))
	override val name: String? get() = SDLVideo.SDL_GetDisplayName(handle)
	override val videoModes: Iterable<VideoMode>
		get() {
		val pArray = SDLVideo.SDL_GetFullscreenDisplayModes(handle) ?: return emptyList()
		return pArray.map(::SDLVideoMode)
	}
	override val videoMode: SDLVideoMode
		get() = SDLVideoMode(SDLVideo.SDL_GetCurrentDisplayMode(handle) ?: throw Exception())

	companion object {

		val primary get() = SDLDisplay(SDLVideo.SDL_GetPrimaryDisplay())
		fun getAllMonitors(): List<SDLDisplay> = SDLVideo.SDL_GetDisplays()?.map(::SDLDisplay) ?: emptyList()
	}
}