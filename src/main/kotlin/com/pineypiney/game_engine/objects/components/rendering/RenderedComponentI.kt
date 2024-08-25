package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2

interface RenderedComponentI : ComponentI {

	var visible: Boolean
	val renderSize: Vec2
	val shape: Shape<*>

	fun render(renderer: RendererI, tickDelta: Double)
}