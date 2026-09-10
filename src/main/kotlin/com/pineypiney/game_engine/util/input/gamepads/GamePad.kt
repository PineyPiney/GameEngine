package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import glm_.and
import glm_.shr
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import kool.cap
import kool.forEachIndexed
import kool.getOrNull
import kool.toByteArray
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

open class GamePad(val id: Int, val inputs: GamepadInput) {

	val numButtons = max(GLFW.glfwGetJoystickButtons(id)?.cap ?: 15, 15)
	val numAxes = GLFW.glfwGetJoystickAxes(id)?.cap ?: 0
	val state = GLFWGamepadState.calloc()

	val name: String? = GLFW.glfwGetGamepadName(id)

	// Joy stick and trigger values
	val axesStates = FloatArray(numAxes)
	val deadzones = FloatArray(numAxes) { .1f }

	// Button values
	val buttonStates = ByteArray(numButtons)

	val leftJoystick get() = Vec2(axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_X], -axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y])
	val rightJoystick
		get() = Vec2(
			axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X],
			-axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y]
		)

	// This function is called every render cycle for every connected gamepad
	fun input() {
		// This function edits state to contain the correct information
		GLFW.glfwGetGamepadState(id, state)

		state.axes().forEachIndexed(::updateAxes)
		state.buttons().forEachIndexed(::updateButton)

		GLFW.glfwGetJoystickButtons(id)?.let { it: ByteBuffer -> updateBonusButtons(it.toByteArray()) }
	}

	fun updateAxes(axis: Int, state: Float) {
		val newValue = if (abs(state) < deadzones[axis]) 0f else state
		val oldValue = axesStates[axis]
		if (oldValue == newValue) return

		axesStates[axis] = newValue

		// This triggers the input function if the axis is pushed in/out of the deadzone
		when {
			newValue > 0f -> if (oldValue <= 0f) inputs.gamepadButtonCallback(InputState(axis.toShort(), ControlType.GAMEPAD_AXIS, 0), 1)
			newValue < 0f -> if (oldValue >= 0f) inputs.gamepadButtonCallback(InputState(axis.toShort(), ControlType.GAMEPAD_AXIS, 0), 2)
			else -> if (oldValue != 0f) inputs.gamepadButtonCallback(InputState(axis.toShort(), ControlType.GAMEPAD_AXIS, 0), 0)
		}
	}

	fun updateButton(button: Int, state: Byte) {
		if (buttonStates[button] == state) return

		buttonStates[button] = state

		inputs.gamepadButtonCallback(InputState(button.toShort(), ControlType.GAMEPAD_BUTTON, 0), state.toInt())
	}

	// GLFW only supports 15 default buttons, but some controllers have extra buttons
	open fun updateBonusButtons(buttons: ByteArray) {

	}

	open fun getButton(button: Int) = buttonStates[button]

	open fun getButtonIcon(type: ControlType, id: Int): Pair<Texture2D, Vec4> {
		return when (type) {
			ControlType.GAMEPAD_BUTTON -> {
				val x = (id % 4) * .25f
				val y = .75f - floor(id * .25f) * .25f
				TextureLoader[ResourceKey("ui/ps_buttons")] to Vec4(x, y, .25f, .25f)
			}

			ControlType.GAMEPAD_AXIS -> {
				Texture2D.missing to Vec4()
			}

			else -> Texture2D.missing to Vec4()
		}
	}

	fun getDpadState(): Byte {
		return GLFW.glfwGetJoystickHats(id)?.getOrNull(0) ?: 0
	}

	fun getDpadVec(): Vec2i {
		val state = getDpadState()
		val x = ((state and GLFW.GLFW_HAT_RIGHT) shr 1) - ((state and GLFW.GLFW_HAT_LEFT) shr 3)
		val y = (state and GLFW.GLFW_HAT_UP) - ((state and GLFW.GLFW_HAT_DOWN) shr 2)
		return Vec2i(x, y)
	}
}