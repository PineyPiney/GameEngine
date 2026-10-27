package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.AXIS_LEFT_X
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.AXIS_LEFT_Y
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.AXIS_RIGHT_X
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.AXIS_RIGHT_Y
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_A
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_B
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_BACK
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_DPAD_DOWN
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_DPAD_LEFT
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_DPAD_RIGHT
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_DPAD_UP
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_GUIDE
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_LEFT_BUMPER
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_LEFT_THUMB
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_OPTIONS
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_RIGHT_BUMPER
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_RIGHT_THUMB
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_X
import com.pineypiney.game_engine.util.input.gamepads.Gamepad.Companion.BUTTON_Y
import kool.iterator
import org.lwjgl.sdl.SDL_GamepadAxisEvent
import org.lwjgl.sdl.SDL_GamepadButtonEvent
import org.lwjgl.sdl.SDL_GamepadDeviceEvent
import org.lwjgl.sdl.SDLGamepad as SDLGamepads

open class SDLGamepadInput(inputs: Inputs) : GamepadInput(inputs) {

	override val connectedGamepads: MutableSet<SDLGamepad> = mutableSetOf()

	init {
		for(id in SDLGamepads.SDL_GetGamepads()!!) {
			val gamepad = createController(id) ?: continue
			connectedGamepads.add(gamepad)
			gamepadConnectCallback(gamepad)
		}
	}

	override fun getController(id: Int): SDLGamepad? = connectedGamepads.firstOrNull { it.id == id }

	fun onControllerConnect(event: SDL_GamepadDeviceEvent) {
		val gamepad = createController(event.which()) ?: return
		connectedGamepads.add(gamepad)
		gamepadConnectCallback(gamepad)
	}

	fun onControllerDisconnect(event: SDL_GamepadDeviceEvent) {
		val gamepad = connectedGamepads.firstOrNull { it.id == event.which() } ?: return
		connectedGamepads.remove(gamepad)
		gamepadDisconnectCallback(gamepad)
		gamepad.delete()
	}

	fun createController(id: Int): SDLGamepad? {
		val handle = SDLGamepads.SDL_OpenGamepad(id)
		return if(handle == 0L) null
		else SDLGamepad(id, handle, this)
	}

	fun onButton(event: SDL_GamepadButtonEvent) {
		val gamepad = getController(event.which())
		gamepad?.updateButton(buttons[event.button()]!!, event.down())
	}

	fun onAxis(event: SDL_GamepadAxisEvent) {
		val gamepad = getController(event.which())
		val axis = event.axis().toInt()
		val value = when(axis) {
			AXIS_LEFT_X, AXIS_RIGHT_X -> event.value() * SS
			AXIS_LEFT_Y, AXIS_RIGHT_Y -> -event.value() * SS
			else -> event.value().toUShort().toInt() * US
		}

		gamepad?.updateAxis(axis, value)
	}

	companion object {

		const val US = 1f / 65535f
		const val SS = 2f / 65535f

		val buttons = mapOf(
			SDLGamepads.SDL_GAMEPAD_BUTTON_SOUTH.toByte() to BUTTON_A,
			SDLGamepads.SDL_GAMEPAD_BUTTON_EAST.toByte() to BUTTON_B,
			SDLGamepads.SDL_GAMEPAD_BUTTON_WEST.toByte() to BUTTON_X,
			SDLGamepads.SDL_GAMEPAD_BUTTON_NORTH.toByte() to BUTTON_Y,
			SDLGamepads.SDL_GAMEPAD_BUTTON_DPAD_UP.toByte() to BUTTON_DPAD_UP,
			SDLGamepads.SDL_GAMEPAD_BUTTON_DPAD_RIGHT.toByte() to BUTTON_DPAD_RIGHT,
			SDLGamepads.SDL_GAMEPAD_BUTTON_DPAD_DOWN.toByte() to BUTTON_DPAD_DOWN,
			SDLGamepads.SDL_GAMEPAD_BUTTON_DPAD_LEFT.toByte() to BUTTON_DPAD_LEFT,

			SDLGamepads.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER.toByte() to BUTTON_LEFT_BUMPER,
			SDLGamepads.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER.toByte() to BUTTON_RIGHT_BUMPER,
			SDLGamepads.SDL_GAMEPAD_BUTTON_LEFT_STICK.toByte() to BUTTON_LEFT_THUMB,
			SDLGamepads.SDL_GAMEPAD_BUTTON_RIGHT_STICK.toByte() to BUTTON_RIGHT_THUMB,
			SDLGamepads.SDL_GAMEPAD_BUTTON_START.toByte() to BUTTON_OPTIONS,
			SDLGamepads.SDL_GAMEPAD_BUTTON_GUIDE.toByte() to BUTTON_GUIDE,
			SDLGamepads.SDL_GAMEPAD_BUTTON_BACK.toByte() to BUTTON_BACK,
		)
	}
}