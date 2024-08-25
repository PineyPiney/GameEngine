package com.pineypiney.game_engine.resources.models.pgm

import com.pineypiney.game_engine.util.Copyable
import glm_.mat4x4.Mat4

data class Controller(
	val id: String,
	val name: String,
	val meshName: String,
	val matrix: Mat4,
	val weights: MutableList<Map<String, Float>>
) {


	data class BoneWeight(val id: Int, val boneName: String, val weight: Float = 0f) : Copyable<BoneWeight> {

		override fun copy() = BoneWeight(id, boneName, weight)
	}
}