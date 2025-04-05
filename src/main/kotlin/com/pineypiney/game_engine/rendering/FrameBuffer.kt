package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.textures.TextureParameters
import glm_.i
import glm_.vec2.Vec2t
import kool.ByteBuffer
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

open class FrameBuffer(var width: Int, var height: Int, var format: Int = GL_RGB, var internalFormat: Int = format) :
	Deleteable {

	constructor(size: Vec2t<*>, format: Int = GL_RGB, internalFormat: Int = format) : this(size.x.i, size.y.i, format, internalFormat)

	val FBO = glGenFramebuffers()
	val TCB = glGenTextures()
	val RBO = glGenRenderbuffers()

	val parameters = TextureParameters()

	open fun setSize(width: Int, height: Int) {
		if (width > 0 && height > 0 && (width != this.width || height != this.height)) {
			this.width = width
			this.height = height
			generate()
		}
	}

	fun setSize(size: Vec2t<*>) {
		setSize(size.x.i, size.y.i)
	}

	open fun generate() {
		glBindFramebuffer(GL_FRAMEBUFFER, FBO)
		glBindTexture(GL_TEXTURE_2D, TCB)

		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, null as ByteBuffer?)
		TextureLoader.loadIndividualSettings(TCB, parameters)
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, TCB, 0)


		glBindRenderbuffer(GL_RENDERBUFFER, RBO)
		glRenderbufferStorage(
			GL_RENDERBUFFER,
			GL_DEPTH24_STENCIL8,
			width,
			height
		) // use a single renderbuffer object for both a depth AND stencil buffer.
		glFramebufferRenderbuffer(
			GL_FRAMEBUFFER,
			GL_DEPTH_STENCIL_ATTACHMENT,
			GL_RENDERBUFFER,
			RBO
		) // now actually attach it
		glBindRenderbuffer(GL_RENDERBUFFER, 0)

		val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
		if (status != GL_FRAMEBUFFER_COMPLETE) GameEngineI.error("Framebuffer could not be completed, status was $status")
		glBindFramebuffer(GL_FRAMEBUFFER, 0)
	}

	open fun bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, FBO)
	}

	open fun draw(shape: Mesh = Mesh.screenQuadShape) {
		glActiveTexture(GL_TEXTURE0)
		glBindTexture(GL_TEXTURE_2D, TCB)
		shape.bindAndDraw()
	}

	fun savePNG(file: String): Boolean {
		val d = getTextureData()
		d.limit(d.capacity())
		val numChannels = TextureLoader.formatToChannels(format)
		val fileName = if (file.endsWith(".png")) file else "$file.png"
		STBImageWrite.stbi_flip_vertically_on_write(true)
		return STBImageWrite.stbi_write_png(fileName, width, height, numChannels, d, numChannels * width)
	}

	fun getTextureData(): ByteBuffer {
		glBindTexture(GL_TEXTURE_2D, TCB)
		val buffer = ByteBuffer(width * height * TextureLoader.formatToChannels(format))
		glGetTexImage(GL_TEXTURE_2D, 0, format, GL_UNSIGNED_BYTE, buffer)
		return buffer
	}

	override fun delete() {
		glDeleteFramebuffers(FBO)
		glDeleteTextures(TCB)
		glDeleteRenderbuffers(RBO)
	}

	companion object {
		/**
		 * Unbind framebuffers, so that things are now drawn onto the screen
		 */
		fun unbind() = glBindFramebuffer(GL_FRAMEBUFFER, 0)
	}
}