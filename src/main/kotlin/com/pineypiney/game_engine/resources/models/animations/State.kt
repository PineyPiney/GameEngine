package com.pineypiney.game_engine.resources.models.animations

import com.pineypiney.game_engine.resources.models.Model

abstract class State(val parentId: String) {

	abstract fun lerpWith(nextState: State, delta: Number): State
	abstract fun applyTo(model: Model)
}