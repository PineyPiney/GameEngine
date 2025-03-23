package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.window.Viewport

interface UpdatingAspectRatioComponent : ComponentI {

	fun updateAspectRatio(view: Viewport)
}