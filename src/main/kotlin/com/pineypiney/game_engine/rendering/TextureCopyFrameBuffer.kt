package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30C.*

class TextureCopyFrameBuffer() : Initialisable{

	val FBO = glGenFramebuffers()
	var srcSize: Vec2i = Vec2i()
	var dstSize = Vec2i()

	override fun init() {
		bind()
		glReadBuffer(GL_COLOR_ATTACHMENT0)
		glDrawBuffer(GL_COLOR_ATTACHMENT1)
	}

	fun bind() = glBindFramebuffer(GL_FRAMEBUFFER, FBO)

	fun setSrc(src: Texture){
		srcSize = src.size
		glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, src.texturePointer, 0)
	}

	fun setDst(dst: Texture){
		dstSize = dst.size
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, dst.texturePointer, 0)
	}

	fun copyTexture(srcOrigin: Vec2i = Vec2i(0), srcTR: Vec2i = srcSize, dstOrigin: Vec2i = Vec2i(0), dstTR: Vec2i = dstSize, mask: Int = GL_COLOR_BUFFER_BIT, filter: Int = GL_LINEAR){
		glBlitFramebuffer(srcOrigin.x, srcOrigin.y, srcTR.x, srcTR.y, dstOrigin.x, dstOrigin.y, dstTR.x, dstTR.y, mask, filter)
	}

	fun copyOntoDst(src: Texture, dstOrigin: Vec2i = Vec2i(0), dstTR: Vec2i = dstOrigin + src.size){
		setSrc(src)
		copyTexture(dstOrigin = dstOrigin, dstTR = dstTR)
	}

	override fun delete() {
		glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 0, 0)
		glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, 0, 0)
		Framebuffer.unbind()
		glDeleteFramebuffers(FBO)
	}
}