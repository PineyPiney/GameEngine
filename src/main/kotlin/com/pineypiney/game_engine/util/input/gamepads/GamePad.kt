package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import glm_.has
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW
import kotlin.math.abs

abstract class GamePad : Deletable {


	abstract val name: String?
	abstract val id: Int
	abstract val input: GamepadInput

	var buttons = 0

	// Joy stick and trigger values
	abstract val axesStates: FloatArray
	abstract val deadzones: FloatArray

	// Button values
	open val leftJoystick: Vec2 get() = Vec2(axesStates[AXIS_LEFT_X], axesStates[AXIS_LEFT_Y])
	open val rightJoystick: Vec2 get() = Vec2(axesStates[AXIS_RIGHT_X], axesStates[AXIS_RIGHT_Y])

	// This function is called every render cycle for every connected gamepad
	abstract fun input()

	fun updateButtons(buttonMask: Int) {
		for (i in 0..31) {
			val m = 1 shl i
			if (buttonMask and m != buttons and m) input.gamepadButtonCallback(InputState(m, ControlType.GAMEPAD_BUTTON, 0), (buttonMask shr i) and 1)
		}
		buttons = buttonMask
	}

	fun updateButton(button: Int, state: Boolean) {
		if (state) {
			if (!buttons.has(button)) {
				input.gamepadButtonCallback(InputState(button, ControlType.GAMEPAD_BUTTON, 0), 1)
				buttons = buttons or button
			}
		} else if (buttons.has(button)) {
			input.gamepadButtonCallback(InputState(button, ControlType.GAMEPAD_BUTTON, 0), 0)
			buttons = buttons and button.inv()
		}
	}

	fun updateAxis(axis: Int, state: Float) {
		val newValue = if (abs(state) < deadzones[axis]) 0f else state
		val oldValue = axesStates[axis]
		if (oldValue == newValue) return

		axesStates[axis] = newValue

		// This triggers the input function if the axis is pushed in/out of the deadzone
		when {
			newValue > 0f -> if (oldValue <= 0f) input.gamepadButtonCallback(InputState(axis.toShort(), ControlType.GAMEPAD_AXIS, 0), 1)
			newValue < 0f -> if (oldValue >= 0f) input.gamepadButtonCallback(InputState(axis.toShort(), ControlType.GAMEPAD_AXIS, 0), 2)
			else -> if (oldValue != 0f) input.gamepadButtonCallback(InputState(axis.toShort(), ControlType.GAMEPAD_AXIS, 0), 0)
		}
	}

	open fun getButton(button: Int) = buttons has button

	override fun delete() {}

	companion object {
		const val BUTTON_A = 0x1
		const val BUTTON_B = 0x2
		const val BUTTON_X = 0x4
		const val BUTTON_Y = 0x8
		const val BUTTON_DPAD_UP = 0x10
		const val BUTTON_DPAD_RIGHT = 0x20
		const val BUTTON_DPAD_DOWN = 0x40
		const val BUTTON_DPAD_LEFT = 0x80

		const val BUTTON_LEFT_BUMPER = 0x100
		const val BUTTON_RIGHT_BUMPER = 0x200
		const val BUTTON_LEFT_THUMB = 0x400
		const val BUTTON_RIGHT_THUMB = 0x800
		const val BUTTON_OPTIONS = 0x1000
		const val BUTTON_GUIDE = 0x2000
		const val BUTTON_CROSS = BUTTON_A
		const val BUTTON_CIRCLE = BUTTON_B
		const val BUTTON_SQUARE = BUTTON_X
		const val BUTTON_TRIANGLE = BUTTON_Y

		const val AXIS_LEFT_X = GLFW.GLFW_GAMEPAD_AXIS_LEFT_X
		const val AXIS_LEFT_Y = GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y
		const val AXIS_RIGHT_X = GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X
		const val AXIS_RIGHT_Y = GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y
		const val AXIS_LEFT_TRIGGER = GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER
		const val AXIS_RIGHT_TRIGGER = GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER
	}
}