package com.pineypiney.game_engine.window

import glm_.vec2.Vec2i
import org.lwjgl.sdl.SDLMouse
import org.lwjgl.sdl.SDLSurface
import java.io.InputStream

class SDLCursor(override val handle: Long) : Cursor {

	constructor(texture: InputStream, point: Vec2i) : this(createHandle(texture, point))

	override fun delete() {
		SDLMouse.SDL_DestroyCursor(handle)
	}

	companion object {
		fun createHandle(texture: InputStream, point: Vec2i): Long {
			val surface = SDLWindow.createSurface(texture)
			val handle = SDLMouse.SDL_CreateColorCursor(surface, point.x, point.y)
			SDLSurface.SDL_DestroySurface(surface)
			return handle
		}
	}
}