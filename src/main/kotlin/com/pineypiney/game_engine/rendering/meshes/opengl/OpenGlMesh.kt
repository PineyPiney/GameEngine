package com.pineypiney.game_engine.rendering.meshes.opengl

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.delete
import org.lwjgl.opengl.GL30C.glDeleteVertexArrays
import org.lwjgl.opengl.GL30C.glGenVertexArrays

abstract class OpenGlMesh(val VAO: Int) : Mesh {

	constructor() : this(if (GLFunc.isLoaded) glGenVertexArrays() else -1)

	override fun delete() {
		glDeleteVertexArrays(VAO)
		buffers.delete()
	}
}