package com.pineypiney.game_engine.util.input

import glm_.and
import glm_.i

abstract class Inputs {

	abstract val inputs: Collection<Input>

	var modStates: Byte = 0

	fun input() {
		for (i in inputs) i.update()
	}

	inline fun <reified E : Input> getInput(): E {
		return inputs.filterIsInstance<E>().first()
	}

	inline fun <reified E : Input> getInputOrNull(): E? {
		return inputs.filterIsInstance<E>().firstOrNull()
	}

	fun getMod(mod: Number) = (modStates and mod.i) > 1
}