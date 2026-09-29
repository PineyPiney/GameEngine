package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.jna.GameInput
import com.sun.jna.Pointer
import glm_.getInt
import glm_.has

class NativeXboxController(override val input: NativeGamepadInput, val controller: GameInput.Controller) : GamePad(), RumbleGamepad {

	override val name: String get() = controller.info.displayName
	override val id: Int get() = controller.info.deviceId.getInt(0)
	override val axesStates: FloatArray = FloatArray(6)
	override val deadzones: FloatArray = FloatArray(6) { .1f }
	val p: Pointer get() = controller.p

	val state = GameInput.GamepadState()

	var highFrequency = 0f
	var lowFrequency = 0f
	var leftTrigRumble = 0f
	var rightTrigRumble = 0f

	override fun input() {
		val readError = GameInput.INSTANCE.updateReading(p)
		if (GameInput.processError(readError, "Failed to get current reading for controller")) return

		GameInput.INSTANCE.pollGamepad(p, state.p)

		for ((k, v) in mapping) {
			updateButton(k, state.buttons has v)
		}

		updateAxis(AXIS_LEFT_TRIGGER, state.leftTrigger)
		updateAxis(AXIS_RIGHT_TRIGGER, state.rightTrigger)
		updateAxis(AXIS_LEFT_X, state.leftStickX)
		updateAxis(AXIS_LEFT_Y, state.leftStickY)
		updateAxis(AXIS_RIGHT_X, state.rightStickX)
		updateAxis(AXIS_RIGHT_Y, state.rightStickY)
	}

	override fun setRumble(lowFrequency: Float, highFrequency: Float) {
		this.lowFrequency = lowFrequency
		this.highFrequency = highFrequency
		GameInput.INSTANCE.setRumble(p, lowFrequency, highFrequency, leftTrigRumble, rightTrigRumble)
	}

	fun setTrigger(leftTrigger: Float, rightTrigger: Float) {
		this.leftTrigRumble = leftTrigger
		this.rightTrigRumble = rightTrigger
		GameInput.INSTANCE.setRumble(p, lowFrequency, highFrequency, leftTrigger, rightTrigger)
	}

	override fun delete() {
		state.delete()
	}

	companion object {

		val mapping = mapOf(
			BUTTON_A to GameInput.gamepadA,
			BUTTON_B to GameInput.gamepadB,
			BUTTON_X to GameInput.gamepadX,
			BUTTON_Y to GameInput.gamepadY,
			BUTTON_DPAD_UP to GameInput.gamepadDPadUp,
			BUTTON_DPAD_DOWN to GameInput.gamepadDPadDown,
			BUTTON_DPAD_LEFT to GameInput.gamepadDPadLeft,
			BUTTON_DPAD_RIGHT to GameInput.gamepadDPadRight,
			BUTTON_LEFT_BUMPER to GameInput.gamepadLeftShoulder,
			BUTTON_RIGHT_BUMPER to GameInput.gamepadRightShoulder,
			BUTTON_LEFT_THUMB to GameInput.gamepadLeftThumbstick,
			BUTTON_RIGHT_THUMB to GameInput.gamepadRightThumbstick,
			BUTTON_OPTIONS to GameInput.gamepadMenu,
			BUTTON_GUIDE to GameInput.gamepadView,
		)
	}
}