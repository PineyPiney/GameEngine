package com.pineypiney.game_engine.window

import org.lwjgl.sdl.SDL_DisplayMode

class SDLVideoMode(val sdl: SDL_DisplayMode) : VideoMode {

	constructor(pointer: Long): this(SDL_DisplayMode.create(pointer))

	override val width: Int get() = sdl.w()
	override val height: Int get() = sdl.h()
	override val refreshRate: Float get() = sdl.refresh_rate()
}