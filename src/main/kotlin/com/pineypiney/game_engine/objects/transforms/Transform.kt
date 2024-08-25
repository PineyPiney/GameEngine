package com.pineypiney.game_engine.objects.transforms

import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4

abstract class Transform<P, R, S> : Copyable<Transform<P, R, S>> {

	protected var dirtyModel = true
	protected var model = I

	abstract var position: P
	abstract var rotation: R
	abstract var scale: S

	abstract infix fun translate(move: P)
	abstract infix fun rotate(angle: R)
	abstract infix fun scale(mult: S)

	fun fetchModel(): Mat4 {
		if (dirtyModel) {
			recalculateModel()
			dirtyModel = false
		}
		return model
	}

	protected abstract fun recalculateModel()

	operator fun component1() = position
	operator fun component2() = rotation
	operator fun component3() = scale

	override fun equals(other: Any?): Boolean {
		if (other !is Transform<*, *, *>) return false
		return this.position == other.position &&
				this.rotation == other.rotation &&
				this.scale == other.scale
	}

	override fun hashCode(): Int {
		return this.position.hashCode() + this.rotation.hashCode() + this.scale.hashCode()
	}
}