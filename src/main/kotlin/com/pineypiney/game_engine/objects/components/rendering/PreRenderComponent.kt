package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.rendering.RendererI

interface PreRenderComponent : ComponentI {
	/**
	 * When set to true preRender is only called when the parent has a RenderComponent that is currently visible
	 */
	val whenVisible: Boolean
	fun preRender(renderer: RendererI, tickDelta: Double)
}