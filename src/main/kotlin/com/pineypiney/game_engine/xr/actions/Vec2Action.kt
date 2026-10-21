package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.xr.XrUtil
import glm_.vec2.Vec2
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class Vec2Action(override val name: String, override val displayName: String, subActionPaths: List<Long>) : InputAction<Vec2, XrActionStateVector2f> {

	override lateinit var action: XrAction private set
	override val states: Map<Long, XrActionStateVector2f> = subActionPaths.associateWith { XrActionStateVector2f.calloc().`type$Default`() }

	override fun init(stack: MemoryStack, actionSet: XrActionSet) {
		action = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_VECTOR2F_INPUT, states.keys, name, displayName)
	}

	override fun update(info: XrActionStateGetInfo, session: XrSession, time: Long) {
		info.action(action)
		for((path, state) in states) {
			XrUtil.processResult(XR10.xrGetActionStateVector2f(session, info.subactionPath(path), state), "Failed to get Vec2 Action $name")
		}
	}

	override fun getValue(subActionPath: Long): Vec2 {
		return states[subActionPath]!!.let { Vec2(it.currentState().x(), it.currentState().y()) }
	}
}