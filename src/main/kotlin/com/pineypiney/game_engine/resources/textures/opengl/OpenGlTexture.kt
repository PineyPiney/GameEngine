package com.pineypiney.game_engine.resources.textures.opengl

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL12C
import org.lwjgl.opengl.GL13C
import java.nio.ByteBuffer

abstract class OpenGlTexture(override val id: String, val texturePointer: Int, val target: Int, val binding: Int) : Texture {

	override val width: Int get() = parameter(GL11C.GL_TEXTURE_WIDTH)
	override val height: Int get() = parameter(GL11C.GL_TEXTURE_HEIGHT)
	override val depth: Int get() = parameter(GL12C.GL_TEXTURE_DEPTH)
	val internalFormat: Int get() = parameter(GL11C.GL_TEXTURE_INTERNAL_FORMAT)
	override val format: TextureFormat get() = TextureFormat.fromGlConst(internalFormat) ?: TextureFormat.RGBA8

	override fun getTextureBinding(): Int = binding

	override fun bind() {
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + binding)
		GL11C.glBindTexture(target, texturePointer)
	}

	override fun unbind() {
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + binding)
		GL11C.glBindTexture(target, 0)
	}

	override fun getData(format: TextureFormat): ByteBuffer {
		bind()
		val buffer = BufferUtils.createByteBuffer(width * height * depth * format.pixelSize)
		GL11C.glFinish()
		GL11C.glGetTexImage(target, 0, format.opengl, format.pixelType, buffer)
		return buffer
	}

	fun parameter(param: Int): Int {
		return if (GLFunc.isLoaded) {
			bind()
			GL11C.glGetTexLevelParameteri(target, 0, param)
		} else 0
	}

	override fun delete() {
		unbind()
		GL11C.glDeleteTextures(texturePointer)
	}

	override fun toString(): String {
		return "Texture[$id]"
	}

	override fun equals(other: Any?): Boolean {
		if (other is OpenGlTexture) return this.texturePointer == other.texturePointer
		return false
	}

	override fun hashCode(): Int {
		return this.texturePointer.hashCode()
	}

	companion object {

		fun createPointer(params: TextureParameters = TextureParameters()): Int {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not create texture pointer because OpenGL has not been loaded")
				return -1
			}

			// Create a handle for the texture
			val ptr = GL11C.glGenTextures()

			// Settings
			GL11C.glBindTexture(params.target, ptr)
			params.loadOpenGL()

			return ptr
		}
	}
}