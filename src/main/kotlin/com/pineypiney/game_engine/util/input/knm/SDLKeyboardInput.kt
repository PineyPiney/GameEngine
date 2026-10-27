package com.pineypiney.game_engine.util.input.knm

import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.SDLWindow
import com.pineypiney.game_engine.window.Window
import glm_.has
import org.lwjgl.glfw.GLFW
import org.lwjgl.sdl.SDLKeycode
import org.lwjgl.sdl.SDL_KeyboardEvent
import org.lwjgl.sdl.SDL_TextInputEvent

class SDLKeyboardInput(inputs: Inputs, window: Window) : KeyboardInput(inputs, window) {

	fun onKey(event: SDL_KeyboardEvent){
		val key = SDLWindow.keys[event.scancode()] ?: return
		val mods = convertMod(event.mod().toInt())
		val state = InputState(key, ControlType.KEYBOARD, mods)
		val action = if(event.repeat()) 2 else if(event.down()) 1 else 0
		keyCallback(state, action)
	}

	fun onType(event: SDL_TextInputEvent){
		for(char in event.textString() ?: return) charCallback(char.code)
	}

	fun convertMod(sdlMod: Int): Int {
		var mod = 0
		if(sdlMod has SDLKeycode.SDL_KMOD_SHIFT) mod = mod or GLFW.GLFW_MOD_SHIFT
		if(sdlMod has SDLKeycode.SDL_KMOD_CTRL) mod = mod or GLFW.GLFW_MOD_CONTROL
		if(sdlMod has SDLKeycode.SDL_KMOD_ALT) mod = mod or GLFW.GLFW_MOD_ALT
		if(sdlMod has SDLKeycode.SDL_KMOD_GUI) mod = mod or GLFW.GLFW_MOD_SUPER
		if(sdlMod has SDLKeycode.SDL_KMOD_CAPS) mod = mod or GLFW.GLFW_MOD_CAPS_LOCK
		if(sdlMod has SDLKeycode.SDL_KMOD_NUM) mod = mod or GLFW.GLFW_MOD_NUM_LOCK
		return mod
	}

	override fun update() {

	}

	override fun delete() {

	}
}