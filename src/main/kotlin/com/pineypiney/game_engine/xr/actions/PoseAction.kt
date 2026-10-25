package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.util.extension_functions.hasAll
import com.pineypiney.game_engine.xr.OpenXrSession
import com.pineypiney.game_engine.xr.XrUtil
import com.pineypiney.game_engine.xr.mat4
import glm_.mat4x4.Mat4
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class PoseAction(override val name: String, override val displayName: String, subActionPaths: List<Long>) : InputAction<Mat4, XrActionStatePose> {

	override lateinit var action: XrAction private set
	override val states: Map<Long, XrActionStatePose> = subActionPaths.associateWith { XrActionStatePose.calloc().`type$Default`() }

	// These Spaces should not be destroyed
	lateinit var spaces: Map<Long, XrSpace>
	// The time of the last update
	var time: Long = 0L
	lateinit var baseSpace: XrSpace

	override fun init(stack: MemoryStack, actionSet: XrActionSet) {
		action = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_POSE_INPUT, states.keys, name, displayName)
	}

	override fun bindToSession(stack: MemoryStack, session: OpenXrSession) {
		spaces = states.keys.associateWith { path -> XrUtil.createActionPoseSpace(stack, session.session, action, path) }
	}

	override fun update(info: XrActionStateGetInfo, session: XrSession, time: Long) {
		info.action(action)

		this.time = time
		for((path, state) in states) {
			XrUtil.processResult(XR10.xrGetActionStatePose(session, info.subactionPath(path), state), "Failed to get Pose Action $name")
		}
	}

	override fun getValue(subActionPath: Long): Mat4 {
		val mat = Mat4()
		val state = states[subActionPath] ?: return mat
		if(state.isActive) {
			val space = spaces[subActionPath]!!
			val location = XrSpaceLocation.calloc().`type$Default`()
			val res = XR10.xrLocateSpace(space, baseSpace, time, location)
			if(res == 0 && location.locationFlags().toInt() hasAll (XR10.XR_SPACE_LOCATION_POSITION_VALID_BIT or XR10.XR_SPACE_LOCATION_ORIENTATION_VALID_BIT)) {
				location.pose().mat4(mat)
			}
			else state.isActive(false)
			location.free()
		}
		return mat
	}
}