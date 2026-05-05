package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.GLFunc
import glm_.i
import glm_.vec2.Vec2t
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

open class Framebuffer(var width: Int, var height: Int, var internalFormat: TextureFormat = TextureFormat.RGB8, var binding: Int = 0) :
	Deletable {

	constructor(size: Vec2t<*>, format: TextureFormat = TextureFormat.RGB8) : this(size.x.i, size.y.i, format)

	val FBO: Int
	val TCB: Int
	val RBO: Int

	init {
		if (GLFunc.isLoaded) {
			FBO = glGenFramebuffers()
			TCB = glGenTextures()
			RBO = glGenRenderbuffers()
		} else {
			FBO = 0
			TCB = 0
			RBO = 0
		}
	}

	val parameters = TextureParameters()

	open fun setSize(width: Int, height: Int) {
		if (width > 0 && height > 0 && (width != this.width || height != this.height)) {
			this.width = width
			this.height = height
			if (GLFunc.isLoaded) generate()
		}
	}

	fun setSize(size: Vec2t<*>) {
		setSize(size.x.i, size.y.i)
	}

	open fun generate() {
		glBindFramebuffer(GL_FRAMEBUFFER, FBO)
		glBindTexture(GL_TEXTURE_2D, TCB)
		parameters.loadOpenGL()

		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat.opengl, width, height, 0, internalFormat.openglLayout, internalFormat.pixelType, null as ByteBuffer?)
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

	open fun draw(renderingApi: RenderingApi, shape: Mesh = Mesh.screenQuadShape) {
		glActiveTexture(GL_TEXTURE0)
		glBindTexture(GL_TEXTURE_2D, TCB)
		shape.bindAndDraw(renderingApi)
	}

	fun copyTexture(id: String, params: TextureParameters = TextureParameters()): Texture2D {
		bind()
		val texture = Texture2D.create(id, width, height, internalFormat, params = params)
		texture.bind()
		glCopyTexImage2D(params.target, 0, internalFormat.opengl, 0, 0, width, height, 0)
		return texture
	}

	fun copyTo(texture: Texture2D) {
		bind()
		texture.bind()

		if (texture is OpenGlTexture2D) {
			glCopyTexImage2D(texture.target, 0, internalFormat.opengl, 0, 0, width, height, 0)
		}
	}

	fun savePNG(file: String): Boolean {
		val d = getTextureData(TextureFormat.RGBA8)
		d.limit(d.capacity())
		val numChannels = TextureLoader.formatToChannels(internalFormat)
		val fileName = if (file.endsWith(".png")) file else "$file.png"
		STBImageWrite.stbi_flip_vertically_on_write(true)
		return STBImageWrite.stbi_write_png(fileName, width, height, numChannels, d, numChannels * width)
	}

	fun getTextureData(format: TextureFormat): ByteBuffer {
		glBindTexture(GL_TEXTURE_2D, TCB)
		val buffer = BufferUtils.createByteBuffer(width * height * format.pixelSize)
		glGetTexImage(GL_TEXTURE_2D, 0, format.openglLayout, format.pixelType, buffer)
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