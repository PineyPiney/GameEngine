package com.pineypiney.game_engine.xr

import org.lwjgl.openxr.XR10
import org.lwjgl.openxr.XrHapticVibration
import org.lwjgl.openxr.XrSessionCreateInfo
import org.lwjgl.system.MemoryStack

object XrStructs {

	fun sessionInfo(stack: MemoryStack, systemId: Long): XrSessionCreateInfo {
		return XrSessionCreateInfo.calloc(stack).`type$Default`()
			.createFlags(0)
			.systemId(systemId)
	}

	fun hapticVibration(stack: MemoryStack, amplitude: Float, frequency: Float = XR10.XR_FREQUENCY_UNSPECIFIED, duration: Long = XR10.XR_MIN_HAPTIC_DURATION): XrHapticVibration {
		return XrHapticVibration.calloc(stack).`type$Default`()
			.amplitude(amplitude)
			.duration(duration)
			.frequency(frequency)
	}
}