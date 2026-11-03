package com.pineypiney.game_engine.rendering.meshes.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.delete
import org.lwjgl.opengl.GL30C.*

abstract class OpenGlMesh(val VAO: Int) : Mesh {

	constructor() : this(if (GLFunc.isLoaded) glGenVertexArrays() else -1)

	open fun bind() {
		glBindVertexArray(VAO)
	}

	fun bindAndDraw(api: RenderingApi, mode: Int = GL_TRIANGLES) {
		bind()
		draw(api, mode)
	}

	override fun delete() {
		glDeleteVertexArrays(VAO)
		buffers.delete()
	}
}