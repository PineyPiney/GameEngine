package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.xr.OpenXrSession
import org.lwjgl.system.Struct

interface OutputAction<T, S: Struct<S>> : OpenXrAction<T, S> {

	fun setValue(session: OpenXrSession, subActionPath: Long, value: T)
}