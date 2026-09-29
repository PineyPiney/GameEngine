package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import glm_.and
import glm_.shr
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import kool.ByteBufferIterator
import kool.getOrNull
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWGamepadState

open class GLFWGamePad(val jid: Int, override val input: GamepadInput) : GamePad() {

	override val name: String? = GLFW.glfwGetGamepadName(jid)
	override val id: Int get() = jid

	val state = GLFWGamepadState.calloc()
	override val axesStates: FloatArray = FloatArray(GLFW.glfwGetJoystickAxes(jid)?.capacity() ?: 0)
	override val deadzones: FloatArray = FloatArray(axesStates.size) { .1f }

	override val leftJoystick: Vec2 get() = Vec2(axesStates[AXIS_LEFT_X], axesStates[AXIS_LEFT_Y])
	override val rightJoystick: Vec2 get() = Vec2(axesStates[AXIS_RIGHT_X], axesStates[AXIS_RIGHT_Y])

	override fun input() {
		// This function fills state with contain the correct information
		GLFW.glfwGetGamepadState(jid, state)

		var buttonMask = 0
		for ((i, b) in ByteBufferIterator(state.buttons()).withIndex()) {
			if (b > 0) buttonMask = buttonMask or (1 shl i)
			val m = 1 shl i
			if (buttonMask and m != buttons and m) input.gamepadButtonCallback(InputState(m, ControlType.GAMEPAD_BUTTON, 0), b.toInt())
		}
		buttons = buttonMask

		updateAxis(0, state.axes(0))
		updateAxis(1, -state.axes(1))
		updateAxis(2, state.axes(2))
		updateAxis(3, -state.axes(3))
		updateAxis(4, .5f * (1f + state.axes(4)))
		updateAxis(5, .5f * (1f + state.axes(5)))

//		GLFW.glfwGetJoystickButtons(id)?.let { it: ByteBuffer -> updateBonusButtons(it.toByteArray()) }
	}

	// GLFW only supports 15 default buttons, but some controllers have extra buttons
	open fun updateBonusButtons(buttons: ByteArray) {

	}

	fun getDpadState(): Byte {
		return GLFW.glfwGetJoystickHats(jid)?.getOrNull(0) ?: 0
	}

	fun getDpadVec(): Vec2i {
		val state = getDpadState()
		val x = ((state and GLFW.GLFW_HAT_RIGHT) shr 1) - ((state and GLFW.GLFW_HAT_LEFT) shr 3)
		val y = (state and GLFW.GLFW_HAT_UP) - ((state and GLFW.GLFW_HAT_DOWN) shr 2)
		return Vec2i(x, y)
	}

	override fun delete() {
		state.free()
	}
}