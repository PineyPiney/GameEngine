package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.rendering.RendererI

interface PreRenderComponent : ComponentI {
	val whenVisible: Boolean
	fun preRender(renderer: RendererI, tickDelta: Double)
}