package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlMesh
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import glm_.i
import glm_.vec2.Vec2i
import glm_.vec2.Vec2t
import org.lwjgl.opengl.GL30C.*

open class OpenGlFramebuffer(
	var width: Int,
	var height: Int,
	colourFormat: TextureFormat = TextureFormat.RGB8,
	depthStencilFormat: TextureFormat = TextureFormat.DEPTH24_STENCIL8,
) : Framebuffer {

	constructor(size: Vec2t<*>, format: TextureFormat = TextureFormat.RGB8) : this(size.x.i, size.y.i, format)

	val params = TextureParameters(mipMapRange = 0..0)

	val FBO: Int = glGenFramebuffers()
	final override val colour: OpenGlTexture2D
	final override val depthStencil: OpenGlTexture2D

	init {
		var p = OpenGlTexture2D.createPointer(null, colourFormat, width, height, params = params)
		colour = OpenGlTexture2D("Framebuffer Colour Texture", p)

		p = OpenGlTexture2D.createPointer(null, depthStencilFormat, width, height, params = params)
		depthStencil = OpenGlTexture2D("Framebuffer Depth Texture", p)
	}

	override val size: Vec2i get() = Vec2i(width, height)

	override fun init() {
		generate()
	}

	override fun setSize(width: Int, height: Int) {
		if (width > 0 && height > 0 && (width != this.width || height != this.height)) {
			this.width = width
			this.height = height
			generate()
		}
	}

	override fun setSize(size: Vec2i) {
		setSize(size.x, size.y)
	}

	open fun generate() {
		glBindFramebuffer(GL_FRAMEBUFFER, FBO)

		colour.setSize(size, params)
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colour.texturePointer, 0)

		depthStencil.setSize(size, params)
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, depthStencil.texturePointer, 0)


		val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
		if (status != GL_FRAMEBUFFER_COMPLETE) GameEngineI.error("Framebuffer could not be completed, status was $status")
		glBindFramebuffer(GL_FRAMEBUFFER, 0)
	}

	open fun bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, FBO)
	}

	open fun draw(renderingApi: RenderingApi, mesh: Mesh = Mesh.screenQuadShape) {
		colour.bind()
		(mesh as OpenGlMesh).bindAndDraw(renderingApi)
	}

	fun copyTexture(id: String, params: TextureParameters = TextureParameters()): OpenGlTexture2D {
		bind()
		val texture = OpenGlTexture2D(id, OpenGlTexture2D.createPointer(null, colour.format, width, height, colour.internalFormat, params))
		texture.bind()
		glCopyTexImage2D(texture.target, 0, texture.internalFormat, 0, 0, width, height, 0)
		return texture
	}

	fun copyTo(texture: OpenGlTexture2D) {
		bind()
		texture.bind()
		glCopyTexImage2D(texture.target, 0, texture.internalFormat, 0, 0, width, height, 0)
	}

	override fun delete() {
		glDeleteFramebuffers(FBO)
		colour.delete()
		depthStencil.delete()
	}

	companion object {
		/**
		 * Unbind framebuffers, so that things are now drawn onto the screen
		 */
		fun unbind() = glBindFramebuffer(GL_FRAMEBUFFER, 0)
	}
}