package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.i
import glm_.s
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import kool.ByteBuffer
import kool.cap
import kool.forEachIndexed
import kool.toByteArray
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState
import kotlin.math.abs
import kotlin.math.floor

open class GamePad(val id: Int, val inputs: Inputs) {

	val numButtons = GLFW.glfwGetJoystickButtons(id)?.cap ?: 0
	val numAxes = GLFW.glfwGetJoystickAxes(id)?.cap ?: 0
	val state = GLFWGamepadState(ByteBuffer(GLFWGamepadState.SIZEOF))

	val name = GLFW.glfwGetGamepadName(id)

	// Joy stick and trigger values
	val axesStates = FloatArray(numAxes)

	// Button values
	val buttonStates = ByteArray(numButtons)

	val deadzone = Vec2(.1f, .25f)

	val leftJoystick get() = Vec2(axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_X], -axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y])
	val rightJoystick
		get() = Vec2(
			axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X],
			-axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y]
		)
	val dPad
		get() = Vec2(
			if (buttonStates[GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT] > 0) -1f else if (buttonStates[GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT] > 0) 1f else 0,
			if (buttonStates[GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN] > 0) -1f else if (buttonStates[GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP] > 0) 1f else 0
		)

	// This function is called every render cycle for every connected gamepad
	fun input() {
		// This function edits state to contain the correct information
		GLFW.glfwGetGamepadState(id, state)

		state.axes().forEachIndexed(::updateAxes)

		state.buttons().forEachIndexed(::updateButton)
		GLFW.glfwGetJoystickButtons(id)?.let { updateBonusButtons(it.toByteArray()) }
	}

	fun updateAxes(axis: Int, state: Float) {
		if (axesStates[axis] == state) return

		if (abs(state) < deadzone.y && abs(axesStates[axis]) < deadzone.y) {
			axesStates[axis] = 0f
			return
		}

		val oldValue = axesStates[axis]
		axesStates[axis] = state

		// This triggers the input function if the axis is pushed past half way
		if (oldValue < deadzone.y && state > deadzone.y) inputs.onInput(axis.s, 1, ControlType.GAMEPAD_AXIS)
		else if (oldValue > -deadzone.y && state < -deadzone.y) inputs.onInput(axis.s, 2, ControlType.GAMEPAD_AXIS)
		else if (abs(oldValue) > deadzone.y && abs(state) < deadzone.y) inputs.onInput(
			axis.s,
			0,
			ControlType.GAMEPAD_AXIS
		)

	}

	fun updateButton(button: Int, state: Byte) {
		if (buttonStates[button] == state) return

		buttonStates[button] = state

		inputs.onInput(button.s, state.i, ControlType.GAMEPAD_BUTTON)
	}

	// GLFW only supports 15 default buttons, but some controllers have extra buttons
	open fun updateBonusButtons(buttons: ByteArray) {

	}

	open fun getButton(button: Int) = buttonStates[button]

	open fun getButtonIcon(type: ControlType, id: Int): Pair<Texture, Vec4> {
		return when (type) {
			ControlType.GAMEPAD_BUTTON -> {
				val x = (id % 4) * .25f
				val y = .75f - floor(id * .25f) * .25f
				TextureLoader[ResourceKey("ui/ps_buttons")] to Vec4(x, y, .25f, .25f)
			}

			ControlType.GAMEPAD_AXIS -> {
				Texture.broke to Vec4()
			}

			else -> Texture.broke to Vec4()
		}
	}
}