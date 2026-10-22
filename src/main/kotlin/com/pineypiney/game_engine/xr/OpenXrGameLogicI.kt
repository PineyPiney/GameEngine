package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.rendering.openxr.OpenXrRendererI

interface OpenXrGameLogicI : GameLogicI {
	override val renderer: OpenXrRendererI<*>
	fun processActions(session: OpenXrSession, time: Long)
}