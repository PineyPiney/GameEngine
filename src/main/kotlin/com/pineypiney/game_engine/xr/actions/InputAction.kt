package com.pineypiney.game_engine.xr.actions

import org.lwjgl.openxr.XrActionStateGetInfo
import org.lwjgl.openxr.XrSession
import org.lwjgl.system.Struct

interface InputAction<T, S: Struct<S>> : OpenXrAction<T, S> {

	fun update(info: XrActionStateGetInfo, session: XrSession, time: Long)
	fun getValue(subActionPath: Long) : T
}