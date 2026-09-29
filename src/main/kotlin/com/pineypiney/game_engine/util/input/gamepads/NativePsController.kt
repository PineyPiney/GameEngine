package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.util.extension_functions.coerceIn
import com.pineypiney.game_engine.util.extension_functions.rotate
import com.pineypiney.game_engine.util.jna.LibScePad
import com.pineypiney.game_engine.util.jna.StdC
import glm_.has
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class NativePsController(override val input: NativeGamepadInput, val userId: Int, val pad: Int) : GamePad(), RumbleGamepad, LightBarGamepad, TriggerEffectGamepad {

	override val name: String
	override val id: Int get() = pad
	override val axesStates: FloatArray = FloatArray(6)
	override val deadzones: FloatArray = FloatArray(6) { .1f }
	val touchPad = mutableMapOf<Byte, Vec2>()

	var orientation: Quat = Quat()
	var acceleration: Vec3 = Vec3()
	var angularVelocity: Vec3 = Vec3()

	var gravityCalibration: Vec3 = Vec3(0f, -1f, 0f)
	var orientationCalibration: Quat = Quat()

	val velocity: Vec3 = Vec3()
	val position: Vec3 = Vec3()

	var connected = false
	val data = LibScePad.Data()
	val info: LibScePad.Info

	init {
		val p = StdC.INSTANCE.malloc(28)
		LibScePad.INSTANCE.scePadGetControllerInformation(pad, p)
		info = LibScePad.Info.read(p)

		LibScePad.INSTANCE.scePadGetControllerType(pad, p)
		name = when (p.getInt(0)) {
			1 -> "Dualshock 4"
			2 -> "Dualsense"
			else -> "Unknown"
		}

		StdC.INSTANCE.free(p)
	}

	override fun input() {
		connected = LibScePad.INSTANCE.scePadReadState(pad, data.p) == 0 && data.connected
		if (!connected) return

		for ((k, v) in mapping) {
			updateButton(k, data.buttonBitmask has v)
		}

		updateAxis(AXIS_LEFT_TRIGGER, data.l2Analogue.toUByte().toInt() * _255)
		updateAxis(AXIS_RIGHT_TRIGGER, data.r2Analogue.toUByte().toInt() * _255)
		updateAxis(AXIS_LEFT_X, (data.leftStick.x.toUByte().toInt() * _127) - 1f)
		updateAxis(AXIS_LEFT_Y, 1f - (data.leftStick.y.toUByte().toInt() * _127))
		updateAxis(AXIS_RIGHT_X, (data.rightStick.x.toUByte().toInt() * _127) - 1f)
		updateAxis(AXIS_RIGHT_Y, 1f - (data.rightStick.y.toUByte().toInt() * _127))

		updateTouchPad()

		data.orientation.times(orientationCalibration, orientation)
		acceleration = data.acceleration
		angularVelocity = data.angularVelocity

		velocity += (getRelativeAcceleration() * Timer.frameDelta)
		position += (velocity * Timer.frameDelta)
	}

	fun updateTouchPad() {
		val resolution = Vec2(info.touchPadInfo.resolution)
		touchPad.clear()
		repeat(data.touchData.touchNum.toInt()) { i ->
			touchPad[data.touchData.touch[i].id] = Vec2(data.touchData.touch[i].pos) / resolution
		}
	}

	fun calibrate() {
		data.orientation.inverse(orientationCalibration)
		gravityCalibration(-data.acceleration)
		velocity(0f)
		position(0f)
	}

	fun getRelativeAcceleration(): Vec3 {
		val gravityOffset = gravityCalibration.rotate(orientation.inverse())
		return acceleration + gravityOffset
	}

	override fun setRumble(lowFrequency: Float, highFrequency: Float) {
		val rumble = LibScePad.Vibration((lowFrequency * 255f).toInt(), (highFrequency * 255f).toInt())
		LibScePad.INSTANCE.scePadSetVibration(pad, rumble.p)
		LibScePad.INSTANCE.scePadSetVibrationMode(pad, LibScePad.RUMBLE_MODE)
		rumble.delete()
	}

	override fun setLightBar(colour: Vec3) {
		val light = LibScePad.LightBar(colour.coerceIn(Vec3(0f), Vec3(1f)))
		LibScePad.INSTANCE.scePadSetLightBar(pad, light.p)
		light.delete()
	}

	override fun setLeftTriggerEffect(triggerEffect: LibScePad.TriggerEffectType) {
		val effect = LibScePad.TriggerEffect(
			1,
			arrayOf(triggerEffect, LibScePad.TriggerEffectOff())
		)
		LibScePad.INSTANCE.scePadSetTriggerEffect(pad, effect.p)
		effect.delete()
	}

	override fun setRightTriggerEffect(triggerEffect: LibScePad.TriggerEffectType) {
		val effect = LibScePad.TriggerEffect(
			2,
			arrayOf(LibScePad.TriggerEffectOff(), triggerEffect)
		)
		LibScePad.INSTANCE.scePadSetTriggerEffect(pad, effect.p)
		effect.delete()
	}

	override fun setTriggerEffects(leftTrigger: LibScePad.TriggerEffectType, rightTrigger: LibScePad.TriggerEffectType) {
		val effect = LibScePad.TriggerEffect(
			3,
			arrayOf(leftTrigger, rightTrigger)
		)
		LibScePad.INSTANCE.scePadSetTriggerEffect(pad, effect.p)
		effect.delete()
	}

	fun setVolumeGain(speaker: Byte, headset: Byte, mic: Byte) {
		val volume = LibScePad.VolumeGain(speaker, headset, mic)
		LibScePad.INSTANCE.scePadSetVolumeGain(pad, volume.p)
		volume.delete()

	}

	override fun delete() {
		data.delete()
	}

	companion object {

		const val _255 = 1f / 255f
		const val _127 = 2f / 255f

		val mapping = mapOf(
			BUTTON_A to LibScePad.BUTTON_CROSS,
			BUTTON_B to LibScePad.BUTTON_CIRCLE,
			BUTTON_X to LibScePad.BUTTON_SQUARE,
			BUTTON_Y to LibScePad.BUTTON_TRIANGLE,
			BUTTON_DPAD_UP to LibScePad.BUTTON_N_DPAD,
			BUTTON_DPAD_DOWN to LibScePad.BUTTON_S_DPAD,
			BUTTON_DPAD_LEFT to LibScePad.BUTTON_W_DPAD,
			BUTTON_DPAD_RIGHT to LibScePad.BUTTON_E_DPAD,
			BUTTON_LEFT_BUMPER to LibScePad.BUTTON_L1,
			BUTTON_RIGHT_BUMPER to LibScePad.BUTTON_R1,
			BUTTON_LEFT_THUMB to LibScePad.BUTTON_L3,
			BUTTON_RIGHT_THUMB to LibScePad.BUTTON_R3,
			BUTTON_OPTIONS to LibScePad.BUTTON_OPTIONS,
			BUTTON_GUIDE to LibScePad.BUTTON_TOUCH,
		)
	}
}