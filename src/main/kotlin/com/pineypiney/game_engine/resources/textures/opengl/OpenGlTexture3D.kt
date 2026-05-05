package com.pineypiney.game_engine.resources.textures.opengl

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.GLFunc
import kool.Buffer
import kool.lim
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45C.*
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class OpenGlTexture3D(id: String, texturePointer: Int, target: Int = GL_TEXTURE_3D, binding: Int = 0) : OpenGlTexture(id, texturePointer, target, binding), Texture3D {

	override fun getSubData(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, format: TextureFormat): ByteBuffer {
		val buffer = BufferUtils.createByteBuffer(bytes)
		glPixelStorei(GL_PACK_ALIGNMENT, 1)
		glGetTextureSubImage(texturePointer, 0, x, y, z, width, height, depth, format.openglLayout, format.pixelType, buffer)
		return buffer
	}

	override fun setData(data: ByteBuffer, format: TextureFormat) {
		bind()
		if (data.lim != width * height * depth * numChannels) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
		glTexImage3D(target, 0, this.format.opengl, width, height, depth, 0, format.openglLayout, format.pixelType, buf)

		MemoryUtil.memFree(buf)
	}

	override fun setSubData(data: ByteBuffer, x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, format: TextureFormat) {
		bind()
		if (data.lim != width * height * depth * numChannels) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
		glTexSubImage3D(target, 0, x, y, z, width, height, depth, format.openglLayout, format.pixelType, buf)

		MemoryUtil.memFree(buf)
	}

	override fun clear() {
//		val PBO = glGenBuffers()
//		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, PBO)
//		glBufferData(GL_PIXEL_UNPACK_BUFFER, bytes.toLong(), GL_STREAM_DRAW)
		bind()
		glTexImage3D(target, 0, internalFormat, width, height, depth, 0, GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4, BufferUtils.createByteBuffer(width * height * depth * 2))
	}

	override fun setSamples(samples: Int, fixedSample: Boolean) {
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texturePointer)
		glTexImage3DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, format.opengl, width, height, depth, fixedSample)
	}

	companion object {

		fun writeTexture3DToPointer(data: ByteBuffer?, dataFormat: TextureFormat, width: Int, height: Int, depth: Int, internalFormat: Int, debug: Boolean = false) {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not write texture to pointer because OpenGL has not been loaded")
				return
			}

			if (debug) GameEngineI.debug("Calling texImage3D with internalFormat: $internalFormat, width: $width, height: $height, depth: $depth, dataFormat: $dataFormat and data: $data")
			try {
				glTexImage3D(GL_TEXTURE_3D, 0, internalFormat, width, height, depth, 0, dataFormat.openglLayout, dataFormat.pixelType, data)
			} catch (_: Exception) {
				GameEngineI.error("Failed to create texture")
			}

			glGenerateMipmap(GL_TEXTURE_3D)
		}

		fun createPointer(
			data: ByteBuffer?,
			dataFormat: TextureFormat,
			width: Int, height: Int, depth: Int,
			internalFormat: Int = dataFormat.opengl,
			params: TextureParameters = TextureParameters(GL_TEXTURE_3D),
			debug: Boolean = false
		): Int {
			val pointer = createPointer(params)
			if (debug) GameEngineI.debug("Calling writeTextureToPointer on texturePtr: $pointer with parameters: $params")
			if (pointer != -1) writeTexture3DToPointer(data, dataFormat, width, height, depth, internalFormat, debug)
			return pointer
		}
	}
}