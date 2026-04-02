package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.util.GLFunc
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL12C.GL_TEXTURE_DEPTH
import org.lwjgl.opengl.GL13C.GL_TEXTURE0
import org.lwjgl.opengl.GL13C.glActiveTexture
import java.nio.ByteBuffer

abstract class TextureI : Resource {

	abstract val id: String
	abstract val texturePointer: Int
	abstract val target: Int
	abstract var binding: Int


	val width: Int get() = parameter(GL_TEXTURE_WIDTH)
	val height: Int get() = parameter(GL_TEXTURE_HEIGHT)
	val depth: Int get() = parameter(GL_TEXTURE_DEPTH)

	val internalFormat: Int get() = parameter(GL_TEXTURE_INTERNAL_FORMAT)
	val format: Int get() = TextureLoader.internalFormatToFormat(internalFormat)
	val numChannels: Int get() = TextureLoader.formatToChannels(format)
	val bytes: Int get() = width * height * TextureLoader.internalFormatToPixelSize(internalFormat)
	val dataType: Int get() = TextureLoader.internalFormatToDataType(internalFormat)

	fun bind() {
		glActiveTexture(GL_TEXTURE0 + binding)
		glBindTexture(target, texturePointer)
	}

	fun unbind() {
		glActiveTexture(GL_TEXTURE0 + binding)
		glBindTexture(target, 0)
	}

	fun getData(type: Int = dataType, pixelSize: Int = 4): ByteBuffer {
		bind()
		val buffer = BufferUtils.createByteBuffer(width * height * depth * pixelSize)
		glFinish()
		glGetTexImage(target, 0, format, type, buffer)
		return buffer
	}

	abstract fun setData(data: ByteBuffer, format: Int = this.format)

	abstract fun clear()

	abstract fun setSamples(samples: Int, fixedSample: Boolean = true)

	fun parameter(param: Int): Int {
		return if (GLFunc.isLoaded) {
			bind()
			glGetTexLevelParameteri(target, 0, param)
		} else 0
	}
}