package com.pineypiney.game_engine.util.input.knm

import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW
import org.lwjgl.sdl.SDLMouse
import org.lwjgl.sdl.SDL_MouseButtonEvent
import org.lwjgl.sdl.SDL_MouseMotionEvent
import org.lwjgl.sdl.SDL_MouseWheelEvent

class SDLMouseInput(inputs: Inputs, window: Window) : MouseInput(inputs, window) {

	fun onCursorMore(event: SDL_MouseMotionEvent){
		val size = window.size
		val xf = event.x()
		val yf = event.y()
		val inv = Vec2(2f / (size.x - 1), -2f / (size.y - 1))

		val screenSpace = Vec2(xf * inv.x - 1f, 1f + yf * inv.y)
		lastPos = CursorPosition(Vec2(screenSpace.x * window.aspectRatio, screenSpace.y), screenSpace, Vec2i(xf, size.y - 1 - yf))

		val moveScreenSpace = Vec2(event.xrel() * inv.x, event.yrel() * inv.y)
		cursorOffset = CursorPosition(Vec2(moveScreenSpace.x * window.aspectRatio, moveScreenSpace.y), moveScreenSpace, Vec2i(event.xrel(), -event.yrel()))

		cursorMoveCallback(lastPos, cursorOffset)
	}

	fun onScroll(event: SDL_MouseWheelEvent) {
		mouseScrollCallback(Vec2(event.x(), event.y()))
	}

	fun onButton(event: SDL_MouseButtonEvent) {
		val glfw = sdl2glfw[event.button()] ?: return
		processMouseButtons(glfw, event.down().i, inputs.modStates)
	}

	companion object {
		val sdl2glfw = mapOf(
			SDLMouse.SDL_BUTTON_LEFT.toByte() to GLFW.GLFW_MOUSE_BUTTON_LEFT.toShort(),
			SDLMouse.SDL_BUTTON_MIDDLE.toByte() to GLFW.GLFW_MOUSE_BUTTON_MIDDLE.toShort(),
			SDLMouse.SDL_BUTTON_RIGHT.toByte() to GLFW.GLFW_MOUSE_BUTTON_RIGHT.toShort(),
			SDLMouse.SDL_BUTTON_X1.toByte() to GLFW.GLFW_MOUSE_BUTTON_4.toShort(),
			SDLMouse.SDL_BUTTON_X2.toByte() to GLFW.GLFW_MOUSE_BUTTON_5.toShort()
		)
	}
}