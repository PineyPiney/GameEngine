package com.pineypiney.game_engine.util.jna

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import glm_.asHexString
import glm_.vec2.Vec2s
import glm_.vec4.Vec4s

interface GameInput : Library {

	// Size: 24 bytes
	data class Controller(val p: Pointer) {
		val device: Pointer get() = p.getPointer(0)
		val lastReading: Pointer get() = p.getPointer(8)
		val info: DeviceInfo get() = DeviceInfo(p.getPointer(16))
	}


	class DeviceInfo(val p: Pointer) {
		val vendorId: Short get() = p.getShort(0)
		val productId: Short get() = p.getShort(2)
		val revisionNumber: Short get() = p.getShort(4)
		val usage: Vec2s get() = p.getVec2s(6)
		val hardwareVersion: Vec4s get() = p.getVec4s(10)
		val firmwareVersion: Vec4s get() = p.getVec4s(18)
		val deviceId: ByteArray get() = p.getByteArray(26, 32)
		val deviceRootId: ByteArray get() = p.getByteArray(58, 32)
		val deviceFamily: Int get() = p.getInt(92)
		val supportedInput: Int get() = p.getInt(96)
		val supportedRumbleMotors: Int get() = p.getInt(100)
		val supportedSystemButtons: Int get() = p.getInt(104)
		val containerId: ByteArray get() = p.getByteArray(108, 16)
		val displayName: String get() = p.getPointer(128).getString(0)
		val pnpPath: String get() = p.getPointer(136).getString(0)

	}

	// Size: 28 bytes
	class GamepadState : Deletable {

		val p = StdC.INSTANCE.malloc(28)

		val buttons get() = p.getInt(0)
		val leftTrigger get() = p.getFloat(4)
		val rightTrigger get() = p.getFloat(8)
		val leftStickX get() = p.getFloat(12)
		val leftStickY get() = p.getFloat(16)
		val rightStickX get() = p.getFloat(20)
		val rightStickY get() = p.getFloat(24)

		override fun delete() {
			StdC.INSTANCE.free(p)
		}
	}

	fun interface ConnectCallback : Callback {
		fun callback(controller: Pointer)
	}

	fun interface DisconnectCallback : Callback {
		fun callback(device: Pointer)
	}

	fun initInput(): Int

	fun setConnectCallback(cb: ConnectCallback)
	fun setDisconnectCallback(cb: DisconnectCallback)

	fun updateReading(controller: Pointer): Int
	fun pollGamepad(controller: Pointer, s: Pointer): Boolean
	fun pollController(controller: Pointer, numButtons: Pointer, buttons: Pointer, numAxes: Pointer, axes: Pointer)

	fun setRumble(controller: Pointer, lowFreq: Float, highFreq: Float, leftTrigger: Float, rightTrigger: Float)

	fun releaseController(controller: Pointer)
	fun releaseInput()


	companion object {
		val INSTANCE: GameInput = Native.load("lib/PGEInput", GameInput::class.java)

		const val gamepadMenu = 0x00000001
		const val gamepadView = 0x00000002
		const val gamepadA = 0x00000004
		const val gamepadB = 0x00000008
		const val gamepadX = 0x00000010
		const val gamepadY = 0x00000020
		const val gamepadDPadUp = 0x00000040
		const val gamepadDPadDown = 0x00000080
		const val gamepadDPadLeft = 0x00000100
		const val gamepadDPadRight = 0x00000200
		const val gamepadLeftShoulder = 0x00000400
		const val gamepadRightShoulder = 0x00000800
		const val gamepadLeftThumbstick = 0x00001000
		const val gamepadRightThumbstick = 0x00002000

		const val DEVICE_DISCONNECTED: Int = 0x838A0001.toInt()
		const val DEVICE_NOT_FOUND: Int = 0x838A0002.toInt()
		const val READING_NOT_FOUND: Int = 0x838A0003.toInt()
		const val REFERENCE_READING_TOO_OLD: Int = 0x838A0004.toInt()
		const val FEEDBACK_NOT_SUPPORTED: Int = 0x838A0007.toInt()
		const val OBJECT_NO_LONGER_EXISTS: Int = 0x838A0008.toInt()
		const val CALLBACK_NOT_FOUND: Int = 0x838A0009.toInt()
		const val HAPTIC_INFO_NOT_FOUND: Int = 0x838A000A.toInt()
		const val AGGREGATE_OPERATION_NOT_SUPPORTED: Int = 0x838A000B.toInt()
		const val INPUT_KIND_NOT_PRESENT: Int = 0x838A000C.toInt()

		const val FAMILY_VIRTUAL = -1
		const val FAMILY_UNKNOWN = 0
		const val FAMILY_XBOX_ONE = 1
		const val FAMILY_XBOX_360 = 2
		const val FAMILY_HID = 3
		const val FAMILY_I8042 = 4
		const val FAMILY_AGGREGATE = 5

		val errorMessages = mapOf(
			DEVICE_DISCONNECTED to "DEVICE_DISCONNECTED",
			DEVICE_NOT_FOUND to "DEVICE_NOT_FOUND",
			READING_NOT_FOUND to "READING_NOT_FOUND",
			REFERENCE_READING_TOO_OLD to "REFERENCE_READING_TOO_OLD",
			FEEDBACK_NOT_SUPPORTED to "FEEDBACK_NOT_SUPPORTED",
			OBJECT_NO_LONGER_EXISTS to "OBJECT_NO_LONGER_EXISTS",
			CALLBACK_NOT_FOUND to "CALLBACK_NOT_FOUND",
			HAPTIC_INFO_NOT_FOUND to "HAPTIC_INFO_NOT_FOUND",
			AGGREGATE_OPERATION_NOT_SUPPORTED to "AGGREGATE_OPERATION_NOT_SUPPORTED",
			INPUT_KIND_NOT_PRESENT to "INPUT_KIND_NOT_PRESENT"
		)

		fun processError(error: Int, message: String): Boolean {
			if (error < 0) {
				GameEngineI.logger.error("$message: ${errorMessages[error] ?: "Unknown Error ${error.asHexString}"}")
				return true
			}
			return false
		}
	}
}