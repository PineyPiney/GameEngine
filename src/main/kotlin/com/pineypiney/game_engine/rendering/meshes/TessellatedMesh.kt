package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.opengl.GL40C

class TessellatedMesh: ArrayMesh {

	val patchVertices: Int

	constructor(vertices: FloatArray, attributes: Map<VertexAttribute<*>, Long>, patchVertices: Int): super(vertices, attributes){
		this.patchVertices = patchVertices
	}

	constructor(parent: ArrayMesh, patchVertices: Int): super(parent.VAO, parent.VBO, parent.attributes, parent.count){
		this.patchVertices = patchVertices
	}

	override fun draw(mode: Int) {
		GLFunc.patchVertices = this.patchVertices
		super.draw(GL40C.GL_PATCHES)
	}
}