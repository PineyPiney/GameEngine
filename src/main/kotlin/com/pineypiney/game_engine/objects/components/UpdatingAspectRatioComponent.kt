package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.rendering.RendererI

interface UpdatingAspectRatioComponent : ComponentI {

	fun updateAspectRatio(renderer: RendererI)
}