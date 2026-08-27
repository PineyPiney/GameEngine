package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.rendering.TextureCopier
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureAspect
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture3D
import com.pineypiney.game_engine.resources.textures.parameters.TextureFilter
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.openglMask
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL30C

class TextureCopyFramebuffer : TextureCopier() {

	val FBO = GLFunc.genFrameBuffer()

	override fun init() {
		bind()
		GL11C.glReadBuffer(GL30C.GL_COLOR_ATTACHMENT0)
		GL11C.glDrawBuffer(GL30C.GL_COLOR_ATTACHMENT1)
	}

	override fun start() {}

	fun bind() = GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, FBO)

	override fun setSrc(src: Texture2D) {
		if (src is OpenGlTexture2D) {
			srcSize = Vec3i(src.size, 1)
			GL30C.glFramebufferTexture2D(GL30C.GL_READ_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, src.target, src.texturePointer, 0)
		}
	}

	override fun setSrc(src: Texture3D, layer: Int) {
		if (src is OpenGlTexture3D) {
			srcSize = src.size
			GL30C.glFramebufferTexture3D(GL30C.GL_READ_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, src.target, src.texturePointer, 0, layer)
		}
	}

	override fun setDst(dst: Texture2D) {
		if (dst is OpenGlTexture2D) {
			dstSize = Vec3i(dst.size, 1)
			GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT1, dst.target, dst.texturePointer, 0)
		}
	}

	override fun setDst(dst: Texture3D, layer: Int) {
		if (dst is OpenGlTexture3D) {
			dstSize = dst.size
			GL30C.glFramebufferTexture3D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT1, dst.target, dst.texturePointer, 0, layer)
		}
	}

	override fun copyTexture(srcOrigin: Vec2i, srcTR: Vec2i, dstOrigin: Vec2i, dstTR: Vec2i, mask: Collection<TextureAspect>, filter: TextureFilter) {
		GL30C.glBlitFramebuffer(srcOrigin.x, srcOrigin.y, srcTR.x, srcTR.y, dstOrigin.x, dstOrigin.y, dstTR.x, dstTR.y, mask.openglMask(), filter.opengl)
	}

	override fun copyTexture(srcOrigin: Vec3i, srcTR: Vec3i, dstOrigin: Vec3i, dstTR: Vec3i, mask: Collection<TextureAspect>, filter: TextureFilter) {

	}

	override fun execute() {}

	override fun delete() {
		Framebuffer.unbind()
		GL30C.glDeleteFramebuffers(FBO)
	}
}