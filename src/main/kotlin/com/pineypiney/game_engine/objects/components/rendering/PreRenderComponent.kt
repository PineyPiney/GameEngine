package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.components.ComponentI

interface PreRenderComponent : ComponentI {
	val whenVisible: Boolean
	fun preRender(tickDelta: Double)
}