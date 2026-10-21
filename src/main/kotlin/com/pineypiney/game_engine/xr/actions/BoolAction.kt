package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class BoolAction(override val name: String, override val displayName: String, subActionPaths: List<Long>) : InputAction<Boolean, XrActionStateBoolean> {

	override lateinit var action: XrAction private set
	override val states: Map<Long, XrActionStateBoolean> = subActionPaths.associateWith { XrActionStateBoolean.calloc().`type$Default`() }

	override fun init(stack: MemoryStack, actionSet: XrActionSet) {
		action = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_BOOLEAN_INPUT, states.keys, name, displayName)
	}

	override fun update(info: XrActionStateGetInfo, session: XrSession, time: Long) {
		info.action(action)
		for((path, state) in states) {
			XrUtil.processResult(XR10.xrGetActionStateBoolean(session, info.subactionPath(path), state), "Failed to get Boolean Action $name")
		}
	}

	override fun getValue(subActionPath: Long): Boolean {
		return states[subActionPath]!!.currentState()
	}
}