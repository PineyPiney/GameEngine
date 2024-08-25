package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.vr.util.*
import glm_.vec3.Vec3
import kool.LongBuffer
import org.lwjgl.openvr.*

// Good example JSON files
// https://github.com/googlevr/tilt-brush/blob/master/actions.json

open class InputVR(val actionFile: String) {

	open fun init() {
		VRInput.VRInput_SetActionManifestPath(actionFile).logInputError()
	}

	fun getActionSetHandle(actionSet: String): Long {
		val lp = LongBuffer(1)
		VRInput.VRInput_GetActionSetHandle(actionSet, lp).logInputError()
		return lp[0]
	}

	fun getActionHandle(action: String): Long {
		val lp = LongBuffer(1)
		VRInput.VRInput_GetActionHandle(action, lp).logInputError()
		return lp[0]
	}

	fun getDigitalActionState(action: Long): Boolean {
		val actionData = InputDigitalActionData.create()
		VRInput.VRInput_GetDigitalActionData(action, actionData, VR.k_ulInvalidInputValueHandle).logInputError()
		return actionData.active && actionData.state
	}

	fun getAnalogActionState(action: Long): Vec3 {
		val actionData = InputAnalogActionData.create()
		VRInput.VRInput_GetAnalogActionData(action, actionData, VR.k_ulInvalidInputValueHandle).logInputError()
		return if (!actionData.active) Vec3()
		else actionData.v
	}

	fun updateActionSet(actionSet: Long) {
		val aSet = VRActiveActionSet.create()
		aSet.actionSet = actionSet
		VRInput.nVRInput_UpdateActionState(aSet.address(), VRActiveActionSet.SIZEOF, 1).logInputError()
	}

	companion object {
		const val invalidSet = VR.k_ulInvalidActionSetHandle
		const val invalid = VR.k_ulInvalidActionHandle
	}
}