package com.pineypiney.game_engine.objects.transforms

import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.I
import glm_.quat.Quat
import glm_.vec3.Vec3

open class Transform3D(position: Vec3 = Vec3(), rotation: Quat = Quat(), scale: Vec3 = Vec3(1)) :
	Transform<Vec3, Quat, Vec3>() {

	override var position: Vec3 = position
		set(value) {
			field = value
			dirtyModel = true
		}
	override var rotation: Quat = rotation
		set(value) {
			field = value
			dirtyModel = true
		}
	override var scale: Vec3 = scale
		set(value) {
			field = value
			dirtyModel = true
		}

	override fun translate(move: Vec3) {
		position plusAssign move
		dirtyModel = true
	}

	override fun rotate(angle: Quat) {
		rotation timesAssign angle
		dirtyModel = true
	}

	infix fun rotate(euler: Vec3) = rotate(Quat(euler))

	override fun scale(mult: Vec3) {
		scale timesAssign mult
		dirtyModel = true
	}

	final override fun recalculateModel() {
		model = (I.translate(position) * rotation.toMat4()).scale(scale)
	}

	override fun copy(): Transform3D = Transform3D(position.copy(), Quat(rotation), scale.copy())

	companion object {
		val origin; get() = Transform3D()
	}
}