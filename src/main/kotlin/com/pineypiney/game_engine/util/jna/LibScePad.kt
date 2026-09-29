package com.pineypiney.game_engine.util.jna

import com.pineypiney.game_engine.objects.Deletable
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import glm_.vec2.Vec2s
import glm_.vec3.Vec3
import kotlin.math.min

// https://github.com/WujekFoliarz/duaLib/blob/master/src/include/duaLib.h
interface LibScePad : Library {

	// Size = 44 Bytes
	class InitParam(allowBT: Boolean = true) : Deletable {

		val p = StdC.INSTANCE.calloc(44, 1)

		init {
			p.setByte(16, if (allowBT) 1 else 0)
		}

		override fun delete() {
			StdC.INSTANCE.free(p)
		}
	}

	abstract class TriggerEffectType {
		abstract fun enum(): TriggerEffectMode
		abstract fun write(pointer: Pointer, offset: Long)
	}

	class TriggerEffectOff : TriggerEffectType() {
		override fun enum(): TriggerEffectMode = TriggerEffectMode.OFF
		override fun write(pointer: Pointer, offset: Long) {}
	}

	class TriggerEffectFeedback(
		val position: Byte = 0,
		val strength: Byte = 0
	) : TriggerEffectType() {
		override fun enum(): TriggerEffectMode = TriggerEffectMode.FEEDBACK
		override fun write(pointer: Pointer, offset: Long) {
			pointer.setByte(offset, position)
			pointer.setByte(offset + 1, strength)
		}
	}

	class TriggerEffectWeapon(
		val startPosition: Byte = 0,
		val endPosition: Byte = 0,
		val strength: Byte = 0
	) : TriggerEffectType() {

		constructor(sp: Int, ep: Int, str: Int) : this(sp.toByte(), ep.toByte(), str.toByte())

		override fun enum(): TriggerEffectMode = TriggerEffectMode.WEAPON
		override fun write(pointer: Pointer, offset: Long) {
			pointer.setByte(offset, startPosition)
			pointer.setByte(offset + 1, endPosition)
			pointer.setByte(offset + 2, strength)
		}
	}

	class TriggerEffectVibration(
		val position: Byte = 0,
		val amplitude: Byte = 0,
		val frequency: Byte = 0
	) : TriggerEffectType() {

		override fun enum(): TriggerEffectMode = TriggerEffectMode.VIBRATION
		override fun write(pointer: Pointer, offset: Long) {
			pointer.setByte(offset, position)
			pointer.setByte(offset + 1, amplitude)
			pointer.setByte(offset + 2, frequency)
		}
	}

	class TriggerEffectMultiPosFeedback(
		val strength: ByteArray = ByteArray(TRIGGER_EFFECT_CONTROL_POINT_NUM)
	) : TriggerEffectType() {

		override fun enum(): TriggerEffectMode = TriggerEffectMode.MULTIPLE_POSITION_FEEDBACK
		override fun write(pointer: Pointer, offset: Long) {
			pointer.write(offset, strength, 0, min(strength.size, TRIGGER_EFFECT_CONTROL_POINT_NUM))
		}
	}

	class TriggerEffectSlopeFeedback(
		val startPosition: Byte = 0,
		val endPosition: Byte = 0,
		val startStrength: Byte = 0,
		val endStrength: Byte = 0
	) : TriggerEffectType() {

		override fun enum(): TriggerEffectMode = TriggerEffectMode.SLOPE_FEEDBACK
		override fun write(pointer: Pointer, offset: Long) {
			pointer.setByte(offset, startPosition)
			pointer.setByte(offset + 1, endPosition)
			pointer.setByte(offset + 2, startStrength)
			pointer.setByte(offset + 3, endStrength)
		}
	}

	class TriggerEffectMultiPosVibration(
		val frequency: Byte = 0,
		val strength: ByteArray = ByteArray(TRIGGER_EFFECT_CONTROL_POINT_NUM)
	) : TriggerEffectType() {

		override fun enum(): TriggerEffectMode = TriggerEffectMode.MULTIPLE_POSITION_VIBRATION
		override fun write(pointer: Pointer, offset: Long) {
			pointer.setByte(offset, frequency)
			pointer.write(offset + 1, strength, 0, min(strength.size, TRIGGER_EFFECT_CONTROL_POINT_NUM))
		}
	}

	// Size: 1 + 7 + (56 * 2) = 120 bytes
	class TriggerEffect(
		triggerMask: Byte = 0,
		command: Array<TriggerEffectType> = Array(2) { TriggerEffectOff() }
	) : Deletable {

		constructor(leftTriggerCommand: TriggerEffectType?, rightTriggerCommand: TriggerEffectType?) : this(
			((if (leftTriggerCommand == null) 0 else 1) or (if (rightTriggerCommand == null) 0 else 2)).toByte(),
			arrayOf(leftTriggerCommand ?: TriggerEffectOff(), rightTriggerCommand ?: TriggerEffectOff())
		)

		val p = StdC.INSTANCE.malloc(120)

		init {
			p.setByte(0, triggerMask)
			p.setInt(8, command[0].enum().i)
			command[0].write(p, 16)
			p.setInt(64, command[1].enum().i)
			command[1].write(p, 72)
		}

		override fun delete() {
			StdC.INSTANCE.free(p)
		}
	}


	class LightBar(
		r: Byte = 0,
		g: Byte = 0,
		b: Byte = 0
	) : Deletable {

		constructor(colour: Vec3) : this(
			(colour.x * 255f).toInt().toUByte().toByte(),
			(colour.y * 255f).toInt().toUByte().toByte(),
			(colour.z * 255f).toInt().toUByte().toByte()
		)

		val p = StdC.INSTANCE.malloc(3)

		var r: Byte
			get() = p.getByte(0)
			set(value) = p.setByte(0, value)
		var g: Byte
			get() = p.getByte(1)
			set(value) = p.setByte(1, value)
		var b: Byte
			get() = p.getByte(2)
			set(value) = p.setByte(2, value)

		init {
			p.setByte(0, r)
			p.setByte(1, g)
			p.setByte(2, b)
		}

		override fun delete() {
			StdC.INSTANCE.free(p)
		}
	}

	// Size = 2 + 2 + 1 + 3 = 8 bytes
	class Touch(
		val pos: Vec2s,
		var id: Byte
	) {

		companion object {
			fun read(pointer: Pointer, offset: Long): Touch {
				return Touch(
					pointer.getVec2s(offset),
					pointer.getByte(offset + 4),
				)
			}
		}
	}

	// Size = 1 + 3 + 4 + (8 * 2) = 24 bytes
	class TouchData(
		val touchNum: Byte,
		val touch: Array<Touch>
	) {

		companion object {
			fun read(pointer: Pointer, offset: Long): TouchData {
				return TouchData(
					pointer.getByte(offset),
					arrayOf(
						Touch.read(pointer, offset + 8),
						Touch.read(pointer, offset + 16)
					)
				)
			}
		}

	}

	class Vibration(
		largeMotor: Byte = 0,
		smallMotor: Byte = 0
	) : Deletable {

		constructor(lm: Int, sm: Int) : this(lm.toByte(), sm.toByte())

		val p = StdC.INSTANCE.calloc(2, 1)

		init {
			p.setByte(0, largeMotor)
			p.setByte(1, smallMotor)
		}

		var largeMotor: Byte
			get() = p.getByte(0)
			set(value) = p.setByte(0, value)
		var smallMotor: Byte
			get() = p.getByte(1)
			set(value) = p.setByte(1, value)

		override fun delete() {
			StdC.INSTANCE.free(p)
		}
	}

	class VolumeGain(
		speakerVolume: Byte = 0,
		headsetVolume: Byte = 0,
		micGain: Byte = 0
	) : Deletable {

		val p = StdC.INSTANCE.calloc(4, 1)

		init {
			p.setByte(0, speakerVolume)
			p.setByte(1, headsetVolume)
			p.setByte(3, micGain)
		}

		override fun delete() {
			StdC.INSTANCE.free(p)
		}
	}

	data class TouchPadInfo(
		val pixelDensity: Float,
		val resolution: Vec2s
	) {

		companion object {
			fun read(p: Pointer, offset: Long = 0): TouchPadInfo {
				return TouchPadInfo(
					p.getFloat(offset),
					p.getVec2s(offset + 4)
				)
			}
		}
	}

	data class StickInfo(
		val deadZoneLeft: Byte,
		val deadZoneRight: Byte
	) {

		companion object {
			fun read(p: Pointer, offset: Long = 0): StickInfo {
				return StickInfo(
					p.getByte(offset),
					p.getByte(offset + 1)
				)
			}
		}
	}

	// Size = 28 bytes
	data class Info(
		val touchPadInfo: TouchPadInfo,
		val stickInfo: StickInfo,
		val connectionType: Byte,
		val connectionCount: Byte,
		val connected: Boolean,
		val deviceClass: DeviceClass
	) {

		companion object {
			fun read(p: Pointer, offset: Long = 0): Info {
				return Info(
					TouchPadInfo.read(p, offset),
					StickInfo.read(p, offset + 8),
					p.getByte(offset + 10),
					p.getByte(offset + 11),
					p.getByte(offset + 12) > 0,
					p.getInt(offset + 16)
				)
			}
		}
	}

	// Size = 4 + 1 + 1 + 10 = 16 bytes
	class ExtUnitData(
		val extUnitId: Int = 0,
		val dataLen: Byte = 0,
		val data: ByteArray = ByteArray(10)
	) {

		companion object {
			fun read(p: Pointer, offset: Long = 0): ExtUnitData {
				return ExtUnitData(
					p.getInt(offset),
					p.getByte(offset + 5),
					p.getByteArray(offset + 6, 10)
				)
			}
		}

	}

	// Size = 120 bytes, bool is 4 bytes
	class Data : Deletable {

		val p = StdC.INSTANCE.malloc(120)

		val buttonBitmask get() = p.getInt(0)
		val leftStick get() = p.getVec2b(4)
		val rightStick get() = p.getVec2b(6)
		val l2Analogue get() = p.getByte(8)
		val r2Analogue get() = p.getByte(9)
		val orientation get() = p.getQuat(12)
		val acceleration get() = p.getVec3(28)
		val angularVelocity get() = p.getVec3(40)
		val touchData get() = TouchData.read(p, 52)
		val connected get() = p.getInt(76) > 0
		val timeStamp get() = p.getLong(80)
		val extUnitData get() = ExtUnitData.read(p, 88)
		val connectionCount get() = p.getByte(104)
		val deviceUniqueDataLen get() = p.getByte(107)
		val deviceUniqueData get() = p.getByteArray(108, 12)

		override fun delete() {
			StdC.INSTANCE.free(p)
		}
	}


	fun scePadInit(): Int
	fun scePadInit3(param: Pointer): Int
	fun scePadTerminate(): Int

	fun scePadOpen(userId: Int, i: Int, j: Int): Int
	fun scePadClose(handle: Int): Int

	fun scePadReadState(handle: Int, p: Pointer): Int
	fun scePadGetContainerIdInformation(handle: Int, p: Pointer): Int
	fun scePadGetControllerBusType(handle: Int, p: Pointer): Int
	fun scePadGetControllerInformation(handle: Int, p: Pointer): Int
	fun scePadGetControllerType(handle: Int, p: Pointer): Int
	fun scePadGetJackState(handle: Int, p: Pointer): Int
	fun scePadGetTriggerEffectState(handle: Int, p: Pointer): Int

	fun scePadSetLightBar(handle: Int, light: Pointer): Int
	fun scePadSetTriggerEffect(handle: Int, triggerEffect: Pointer): Int
	fun scePadSetVibrationMode(handle: Int, mode: Int): Int
	fun scePadSetVibration(handle: Int, vibration: Pointer): Int

	fun scePadSetVolumeGain(handle: Int, volumeGain: Pointer): Int

	fun scePadResetLightBar(handle: Int): Int
	fun scePadResetOrientation(handle: Int): Int

	companion object {
		val INSTANCE = Native.load("libScePad", LibScePad::class.java)

		const val TRIGGER_EFFECT_TRIGGER_L2 = 1
		const val TRIGGER_EFFECT_TRIGGER_R2 = 2
		const val TRIGGER_EFFECT_TRIGGER_NUM = 2
		const val TRIGGER_EFFECT_CONTROL_POINT_NUM = 10

		const val BUTTON_SHARE = 0x00000001
		const val BUTTON_L3 = 0x00000002
		const val BUTTON_R3 = 0x00000004
		const val BUTTON_OPTIONS = 0x00000008
		const val BUTTON_N_DPAD = 0x00000010
		const val BUTTON_E_DPAD = 0x00000020
		const val BUTTON_S_DPAD = 0x00000040
		const val BUTTON_W_DPAD = 0x00000080

		const val BUTTON_L2 = 0x00000100
		const val BUTTON_R2 = 0x00000200
		const val BUTTON_L1 = 0x00000400
		const val BUTTON_R1 = 0x00000800
		const val BUTTON_TRIANGLE = 0x00001000
		const val BUTTON_CIRCLE = 0x00002000
		const val BUTTON_CROSS = 0x00004000
		const val BUTTON_SQUARE = 0x00008000

		const val BUTTON_PSBTN = 0x00010000
		const val BUTTON_TOUCH = 0x00100000

		const val HAPTICS_MODE = 1
		const val RUMBLE_MODE = 2

		const val DEVICE_CLASS_INVALID: DeviceClass = -1
		const val DEVICE_CLASS_STANDARD: DeviceClass = 0
		const val DEVICE_CLASS_GUITAR: DeviceClass = 1
		const val DEVICE_CLASS_DRUM: DeviceClass = 2
		const val DEVICE_CLASS_DJ_TURNTABLE: DeviceClass = 3
		const val DEVICE_CLASS_DANCEMAT: DeviceClass = 4
		const val DEVICE_CLASS_NAVIGATION: DeviceClass = 5
		const val DEVICE_CLASS_STEERING_WHEEL: DeviceClass = 6
		const val DEVICE_CLASS_STICK: DeviceClass = 7
		const val DEVICE_CLASS_FLIGHT_STICK: DeviceClass = 8
		const val DEVICE_CLASS_GUN: DeviceClass = 9

		const val ERROR_INVALID_ARG = 0x80920001.toInt()
		const val ERROR_INVALID_PORT = 0x80920002.toInt()
		const val ERROR_INVALID_HANDLE = 0x80920003.toInt()
		const val ERROR_ALREADY_OPENED = 0x80920004.toInt()
		const val ERROR_NOT_INITIALIZED = 0x80920005.toInt()
		const val ERROR_INVALID_LIGHTBAR_SETTING = 0x80920006.toInt()
		const val ERROR_DEVICE_NOT_CONNECTED = 0x80920007.toInt()
		const val ERROR_NO_HANDLE = 0x80920008.toInt()
		const val ERROR_FATAL = 0x809200FF.toInt()
		const val ERROR_NOT_PERMITTED = 0x80920101.toInt()
		const val ERROR_INVALID_BUFFER_LENGTH = 0x80920102.toInt()
		const val ERROR_INVALID_REPORT_LENGTH = 0x80920103.toInt()
		const val ERROR_INVALID_REPORT_ID = 0x80920104.toInt()
		const val ERROR_SEND_AGAIN = 0x80920105.toInt()
	}

	enum class TriggerEffectMode(val i: Int) {
		OFF(0),
		FEEDBACK(1),
		WEAPON(2),
		VIBRATION(3),
		MULTIPLE_POSITION_FEEDBACK(4),
		SLOPE_FEEDBACK(5),
		MULTIPLE_POSITION_VIBRATION(6),
//		WEAPON_(37)
	}
}

typealias DeviceClass = Int