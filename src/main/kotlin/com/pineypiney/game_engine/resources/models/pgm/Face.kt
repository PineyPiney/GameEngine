package com.pineypiney.game_engine.resources.models.pgm

import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.util.exceptions.ArrayTooSmallException

data class Face @Throws(ArrayTooSmallException::class) constructor(
	val vertices: Array<ModelMesh.MeshVertex>,
	val smoothShading: Boolean = false
) {

	init {
		if (vertices.size < 3) {
			throw ArrayTooSmallException("3 vertices are need to make a face")
		}
	}

	override fun toString(): String {
		return "${vertices[0]}\n${vertices[1]}\n${vertices[2]}\n"
	}
}