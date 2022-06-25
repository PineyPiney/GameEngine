package com.pineypiney.game_engine.objects.game_objects.transforms

import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.maths.I

abstract class Transform<P, R, S>: Copyable<Transform<P, R, S>> {

    var model = I

    abstract var position: P
    abstract var rotation: R
    abstract var scale: S

    abstract fun translate(move: P)
    abstract fun rotate(angle: R)
    abstract fun scale(mult: S)

    protected abstract fun recalculateModel()

    operator fun component1() = position
    operator fun component2() = rotation
    operator fun component3() = scale

    override fun equals(other: Any?): Boolean {
        if(other !is Transform<*, *, *>) return false
        return this.position == other.position &&
                this.rotation == other.rotation &&
                this.scale == other.scale
    }

    override fun hashCode(): Int {
        return this.position.hashCode() + this.rotation.hashCode() + this.scale.hashCode()
    }
}