package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30C.*

open class OpenGlCDSFramebuffer(
	size: Vec2i,
	colourFormat: TextureFormat = TextureFormat.RGB8,
	depthStencilFormat: TextureFormat = TextureFormat.DEPTH24_STENCIL8,
) : OpenGlFramebuffer {

	override val FBO: Int = glGenFramebuffers()

	val params = TextureParameters(mipMapRange = 0..0)

	final override var size: Vec2i = size; private set
	final override val colour: OpenGlTexture2D
	final override val depthStencil: OpenGlTexture2D

	init {
		var p = OpenGlTexture2D.createPointer(null, colourFormat, size.x, size.y, params = params)
		colour = OpenGlTexture2D("Framebuffer Colour Texture", p)

		p = OpenGlTexture2D.createPointer(null, depthStencilFormat, size.x, size.y, params = params)
		depthStencil = OpenGlTexture2D("Framebuffer Depth Texture", p)
	}

	override fun init() {
		generate()
	}

	override fun setSize(width: Int, height: Int) {
		setSize(Vec2i(width, height))
	}

	override fun setSize(size: Vec2i) {
		if(size.x > 0 && size.y > 0 && size.x != this.size.x && size.y != this.size.y) {
			this.size = size
			generate()
		}
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

	override fun delete() {
		glDeleteFramebuffers(FBO)
		colour.delete()
		depthStencil.delete()
	}
}