package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.rendering.opengl.OpenGlFramebuffer
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30C

class OpenXrGlFramebuffer(val colourTextures: List<OpenGlTexture2D>, val depthTextures: List<OpenGlTexture2D>) : OpenGlFramebuffer, OpenXrFramebuffer {

	override val FBO: Int = GL30C.glGenFramebuffers()
	override var colourIndex = 0
	override var depthIndex = 0

	override val colour: OpenGlTexture2D get() = colourTextures[colourIndex]
	override val depthStencil: OpenGlTexture2D get() = depthTextures[depthIndex]
	override val size: Vec2i get() = colour.size

	override fun init() {

	}

	override fun setTextures(colour: Int, depth: Int){
		colourIndex = colour
		depthIndex = depth

		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, FBO)
		GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL30C.GL_TEXTURE_2D, this.colour.texturePointer, 0)
		GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, this.depthStencil.texturePointer, 0)
	}

	override fun delete() {
		GL30C.glDeleteFramebuffers(FBO)
	}
}