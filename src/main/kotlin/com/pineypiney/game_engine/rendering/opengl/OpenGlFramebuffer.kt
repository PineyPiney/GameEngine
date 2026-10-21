package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlMesh
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import org.lwjgl.opengl.GL30C.*

interface OpenGlFramebuffer : Framebuffer {

	val FBO: Int

	override val colour: OpenGlTexture2D
	override val depthStencil: OpenGlTexture2D

	fun bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, FBO)
	}

	fun draw(renderingApi: RenderingApi, mesh: Mesh = Mesh.screenQuadShape) {
		colour.bind()
		(mesh as OpenGlMesh).bindAndDraw(renderingApi)
	}

	fun copyTexture(id: String, params: TextureParameters = TextureParameters()): OpenGlTexture2D {
		bind()
		val texture = OpenGlTexture2D(id, OpenGlTexture2D.createPointer(null, colour.format, size.x, size.y, colour.internalFormat, params))
		texture.bind()
		glCopyTexImage2D(texture.target, 0, texture.internalFormat, 0, 0, size.x, size.y, 0)
		return texture
	}

	fun copyTo(texture: OpenGlTexture2D) {
		bind()
		texture.bind()
		glCopyTexImage2D(texture.target, 0, texture.internalFormat, 0, 0, size.x, size.y, 0)
	}

	companion object {
		/**
		 * Unbind framebuffers, so that things are now drawn onto the screen
		 */
		fun unbind() = glBindFramebuffer(GL_FRAMEBUFFER, 0)
	}
}