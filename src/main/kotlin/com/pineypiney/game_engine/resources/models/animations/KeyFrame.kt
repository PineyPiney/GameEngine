package com.pineypiney.game_engine.resources.models.animations

import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.cerp
import com.pineypiney.game_engine.util.extension_functions.eerp
import com.pineypiney.game_engine.util.extension_functions.querp
import com.pineypiney.game_engine.util.extension_functions.serp

class KeyFrame(val time: Float, val states: Array<State>) : Comparable<KeyFrame>, Copyable<KeyFrame> {

	constructor(time: Float, states: Collection<State>) : this(time, states.toTypedArray())

	fun lerp(next: KeyFrame, delta: Double): Array<State> {
		val lerpStates: MutableList<State> = mutableListOf()
		this.states.forEach { state ->
			val nextState = next.states.firstOrNull { it.parentId == state.parentId }
			if (nextState != null) {
				lerpStates.add(state.lerpWith(nextState, delta))
			}
		}
		return lerpStates.toTypedArray()
	}

	fun serp(next: KeyFrame, delta: Double) = lerp(next, delta.serp())
	fun eerp(next: KeyFrame, delta: Double, exponent: Int) = lerp(next, delta.eerp(exponent))
	fun querp(next: KeyFrame, delta: Double) = lerp(next, delta.querp())
	fun cerp(next: KeyFrame, delta: Double) = lerp(next, delta.cerp())

	override fun copy() = KeyFrame(time, states.copyOf())

	override fun compareTo(other: KeyFrame): Int {
		return this.time.compareTo(other.time)
	}

	operator fun compareTo(time: Double): Int {
		return this.time.compareTo(time)
	}

	override fun toString(): String {
		return "KeyFrame($time, ${states.size})"
	}
}