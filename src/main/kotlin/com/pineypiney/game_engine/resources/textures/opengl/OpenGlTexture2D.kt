package com.pineypiney.game_engine.resources.textures.opengl

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.GLFunc
import kool.Buffer
import kool.lim
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL30C.glGenerateMipmap
import org.lwjgl.opengl.GL32C.GL_TEXTURE_2D_MULTISAMPLE
import org.lwjgl.opengl.GL32C.glTexImage2DMultisample
import org.lwjgl.opengl.GL45C.glGetTextureSubImage
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class OpenGlTexture2D(
	id: String,
	texturePointer: Int,
	target: Int = GL_TEXTURE_2D,
	binding: Int = 0
) : OpenGlTexture(id, texturePointer, target, binding), Texture2D {

	override fun setData(data: ByteBuffer, format: TextureFormat) {
		bind()
		if (data.lim != bytes) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

		glTexImage2D(target, 0, this.format.opengl, width, height, 0, format.openglLayout, format.pixelType, buf)
		MemoryUtil.memFree(buf)
	}

	override fun getSubData(x: Int, y: Int, width: Int, height: Int, format: TextureFormat): ByteBuffer {
		val buffer = BufferUtils.createByteBuffer(bytes)
		glPixelStorei(GL_PACK_ALIGNMENT, 1)
		glGetTextureSubImage(texturePointer, 0, x, y, 0, width, height, 1, format.openglLayout, format.pixelType, buffer)
		return buffer
	}

	override fun setSubData(data: ByteBuffer, x: Int, y: Int, width: Int, height: Int, format: TextureFormat) {
		bind()
		if (data.lim != width * height * this.format.pixelSize) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

		glTexSubImage2D(target, 0, x, y, width, height, format.openglLayout, format.pixelType, buf)
		MemoryUtil.memFree(buf)
	}

	override fun clear() {
//		val PBO = glGenBuffers()
//		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, PBO)
//		glBufferData(GL_PIXEL_UNPACK_BUFFER, bytes.toLong(), GL_STREAM_DRAW)
		bind()
		glTexImage2D(target, 0, internalFormat, width, height, 0, 0, 0, null as ByteBuffer?)
	}

	override fun setSamples(samples: Int, fixedSample: Boolean) {
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texturePointer)
		glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, format.opengl, width, height, fixedSample)
	}

	companion object {

		fun writeTextureToPointer(data: ByteBuffer?, width: Int, height: Int, dataFormat: TextureFormat, internalFormat: Int, debug: Boolean = false) {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not write texture to pointer because OpenGL has not been loaded")
				return
			}

			try {
				glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, dataFormat.openglLayout, dataFormat.pixelType, data)
			} catch (_: Exception) {
				GameEngineI.error("Failed to create texture")
			}

			glGenerateMipmap(GL_TEXTURE_2D)
		}

		fun createPointer(
			data: ByteBuffer?,
			dataFormat: TextureFormat,
			width: Int, height: Int,
			internalFormat: Int = dataFormat.opengl,
			params: TextureParameters = TextureParameters(),
			debug: Boolean = false
		): Int {
			val pointer = createPointer(params)
			if (debug) GameEngineI.debug("Calling writeTextureToPointer on texturePtr: $pointer with parameters: $params")
			if (pointer != -1) writeTextureToPointer(data, width, height, dataFormat, internalFormat, debug)
			return pointer
		}
	}
}