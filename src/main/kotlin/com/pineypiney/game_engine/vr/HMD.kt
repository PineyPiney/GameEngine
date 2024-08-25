package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.vr.util.deviceToAbsoluteTracking
import com.pineypiney.game_engine.vr.util.logCompositorError
import com.pineypiney.game_engine.vr.util.poseIsValid
import glm_.mat4x4.Mat4
import org.lwjgl.openvr.TrackedDevicePose
import org.lwjgl.openvr.VR
import org.lwjgl.openvr.VRCompositor

class HMD(near: Float = 0.1f, far: Float = 100f) : Initialisable {

	val eyes: Array<Mat4> = Array(2) { VRUtil.getEyeToHeadMatrix(it).inverse() }
	val projections: Array<Mat4> = Array(2) { VRUtil.getProjectionMatrix(it, near, far) }

	val poses = TrackedDevicePose.create(maxDevices)
	val mat4Poses = Array(maxDevices) { Mat4() }

	val hmd get() = poses[index]
	val hmdPose = Mat4()

	var shouldRun = true

	override fun init() {

	}

	fun updateHMDMatrix() {
		VRCompositor.VRCompositor_WaitGetPoses(poses, null).logCompositorError()
		for (device in 0 until VR.k_unMaxTrackedDeviceCount) {
			if (poses[device].poseIsValid) {
				mat4Poses[device] = poses[device].deviceToAbsoluteTracking
			}
		}

		if (poses[VR.k_unTrackedDeviceIndex_Hmd].poseIsValid)
			mat4Poses[VR.k_unTrackedDeviceIndex_Hmd] inverse hmdPose
	}

	override fun delete() {

	}

	companion object {
		const val index = VR.k_unTrackedDeviceIndex_Hmd
		const val maxDevices = VR.k_unMaxTrackedDeviceCount
	}
}