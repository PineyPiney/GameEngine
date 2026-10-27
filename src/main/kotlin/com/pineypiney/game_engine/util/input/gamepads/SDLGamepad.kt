package com.pineypiney.game_engine.util.input.gamepads

import glm_.vec3.Vec3
import glm_.vec3.Vec3b
import org.lwjgl.BufferUtils
import org.lwjgl.sdl.SDLSensor
import org.lwjgl.sdl.SDLGamepad as SDLGamepads

class SDLGamepad(override val id: Int, val handle: Long, override val input: SDLGamepadInput) : Gamepad(), LightBarGamepad, RumbleGamepad, TriggerRumbleGamepad, SensorGamepad {

	override val name: String? = SDLGamepads.SDL_GetGamepadName(handle)
	override val axesStates: FloatArray = FloatArray(SDLGamepads.SDL_GAMEPAD_AXIS_COUNT)
	override val deadzones: FloatArray = FloatArray(SDLGamepads.SDL_GAMEPAD_AXIS_COUNT) { .05f }

	val hasAccelerometer = enableSensor(SDLSensor.SDL_SENSOR_ACCEL)
	val hasGyro = enableSensor(SDLSensor.SDL_SENSOR_GYRO)

	override var acceleration = Vec3(0f); private set
	override var angularVelocity = Vec3(0f); private set

	override fun input() {
		if(hasAccelerometer) acceleration = readSensor(SDLSensor.SDL_SENSOR_ACCEL)
		if(hasGyro) angularVelocity = readSensor(SDLSensor.SDL_SENSOR_GYRO)
	}

	override fun setLightBar(colour: Vec3b) {
		SDLGamepads.SDL_SetGamepadLED(handle, colour.x, colour.y, colour.z)
	}

	override fun setRumble(lowFrequency: Float, highFrequency: Float) {
		val ls = (lowFrequency * 65535).toInt().toShort()
		val hs = (highFrequency * 65535).toInt().toShort()
		SDLGamepads.SDL_RumbleGamepad(handle, ls, hs, 0xffff)
	}

	override fun setTriggerRumble(leftRumble: Float, rightRumble: Float) {
		val ls = (leftRumble * 65535).toInt().toShort()
		val rs = (rightRumble * 65535).toInt().toShort()
		SDLGamepads.SDL_RumbleGamepadTriggers(handle, ls, rs, 0xffff)
	}

	fun hasSensor(type: Int) = SDLGamepads.SDL_GamepadHasSensor(handle, type)

	fun enableSensor(type: Int): Boolean {
		return hasSensor(type) && SDLGamepads.SDL_SetGamepadSensorEnabled(handle, type, true)
	}

	fun readSensor(type: Int): Vec3 {
		val data = BufferUtils.createFloatBuffer(3)
		SDLGamepads.SDL_GetGamepadSensorData(handle, type, data)
		return Vec3(data)
	}

	override fun delete() {
		setRumble(0f, 0f)
		setTriggerRumble(0f, 0f)
		SDLGamepads.SDL_CloseGamepad(handle)
	}
}