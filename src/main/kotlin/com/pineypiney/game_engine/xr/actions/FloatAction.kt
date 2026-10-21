package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class FloatAction(override val name: String, override val displayName: String, subActionPaths: List<Long>) : InputAction<Float, XrActionStateFloat> {

	override lateinit var action: XrAction private set
	override val states: Map<Long, XrActionStateFloat> = subActionPaths.associateWith { XrActionStateFloat.calloc().`type$Default`() }

	override fun init(stack: MemoryStack, actionSet: XrActionSet) {
		action = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_FLOAT_INPUT, states.keys, name, displayName)
	}

	override fun update(info: XrActionStateGetInfo, session: XrSession, time: Long) {
		info.action(action)
		for((path, state) in states) {
			XrUtil.processResult(XR10.xrGetActionStateFloat(session, info.subactionPath(path), state), "Failed to get Float Action $name")
		}
	}

	override fun getValue(subActionPath: Long): Float {
		return states[subActionPath]!!.currentState()
	}
}