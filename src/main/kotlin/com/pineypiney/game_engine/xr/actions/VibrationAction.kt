package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.xr.OpenXrSession
import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class VibrationAction(override val name: String, override val displayName: String, subActionPaths: List<Long>) : OutputAction<XrHapticBaseHeader, XrActionStateBoolean> {

	override lateinit var action: XrAction private set
	override val states: Map<Long, XrActionStateBoolean> = subActionPaths.associateWith { XrActionStateBoolean.calloc().`type$Default`() }

	override fun init(stack: MemoryStack, actionSet: XrActionSet) {
		action = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_VIBRATION_OUTPUT, states.keys, name, displayName)
	}

	override fun setValue(session: OpenXrSession, subActionPath: Long, value: XrHapticBaseHeader) {
		val actionInfo = XrHapticActionInfo.calloc().`type$Default`().action(action).subactionPath(subActionPath)
		XrUtil.processResult(XR10.xrApplyHapticFeedback(session.session, actionInfo, value), "Failed to apply haptic feedback")
		actionInfo.free()
	}
}