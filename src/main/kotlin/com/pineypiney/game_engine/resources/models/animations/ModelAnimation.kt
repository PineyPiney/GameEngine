package com.pineypiney.game_engine.resources.models.animations

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import glm_.d
import glm_.f

class ModelAnimation(val name: String, val frames: Array<KeyFrame>) : Copyable<ModelAnimation> {

	val length: Double = frames.maxOf { key -> key.time }.d

	fun getAnimationTime(startTime: Double, time: Double = Timer.frameTime): Double {
		// Prevents infinite looping
		if (length <= 0) return 0.0
		return (time - startTime).mod(length)
	}

	fun getState(animationTime: Double, erp: String): Array<State> {
		if (frames.isEmpty()) return arrayOf()
		if (animationTime == 0.0) return frames.first().states

		// If this time perfectly matches a keyframe just return that KeyFrame's states
		val perfectFrame = frames.firstOrNull { frame -> frame.time == animationTime.f }
		if (perfectFrame != null) {
			return perfectFrame.states
		}

		// 2 Maps of the transform of each parent associated with the time of the state.
		// The list of previous states is first reversed to that it gets the most recent time
		val previousStates = frames.filter { it.time < animationTime }.reversed().flatMap { it.states.toList() }
			.distinctBy { it.parentId }
			.associateWith { state -> frames.firstOrNull { it.states.contains(state) }?.time ?: 0f }
		val nextStates =
			frames.filter { it.time > animationTime }.flatMap { it.states.toList() }.distinctBy { it.parentId }
				.associateWith { state -> frames.firstOrNull { it.states.contains(state) }?.time ?: 0f }

		val lerpStates: MutableList<State> = mutableListOf()
		for ((state, lastTime) in previousStates) {
			val nextState = nextStates.entries.firstOrNull { it.key.parentId == state.parentId }
			if (nextState == null) lerpStates.add(state)
			else {
				val deltaTime = (animationTime - lastTime) / (nextState.value - lastTime)
				lerpStates.add(state.lerpWith(nextState.key, deltaTime))
			}
		}

		return lerpStates.toTypedArray()

		/*
		val lastFrame = frames.lastOrNull { frame -> frame.time <= animationTime } ?: frames[0]
		val nextFrame = frames.getOrElse(frames.indexOf(lastFrame) + 1) { lastFrame }

		val transition = (animationTime - lastFrame.time) / (nextFrame.time - lastFrame.time)

		return when(erp){
			"lerp" -> lastFrame.lerp(nextFrame, transition)
			"serp" -> lastFrame.serp(nextFrame, transition)
			"eerp" -> lastFrame.eerp(nextFrame, transition, 20)
			"querp" -> lastFrame.querp(nextFrame, transition)
			"cerp" -> lastFrame.cerp(nextFrame, transition)
			else -> lastFrame.lerp(nextFrame, transition)
		}

		 */
	}

	override fun copy() = ModelAnimation(name, frames.copy())

	override fun toString(): String {
		return "Animation($name)"
	}
}