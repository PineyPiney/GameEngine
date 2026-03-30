package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.TextureCopyFramebuffer
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.repeat
import glm_.f
import glm_.vec2.Vec2i
import kool.Buffer
import kool.lim
import kool.toBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45C.*
import org.lwjgl.stb.STBImageWrite
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class Texture(
	override val id: String,
	override val texturePointer: Int,
	override val target: Int = GL_TEXTURE_2D,
	override var binding: Int = 0
) : TextureI() {

	val size get() = Vec2i(width, height)
	val aspectRatio get() = width.f / height

	fun getSubData(x: Int, y: Int, width: Int, height: Int, format: Int = this.format): ByteBuffer {
		val buffer = BufferUtils.createByteBuffer(bytes)
		glPixelStorei(GL_PACK_ALIGNMENT, 1)
		glGetTextureSubImage(texturePointer, 0, x, y, 0, width, height, 1, format, dataType, buffer)
		return buffer
	}

	fun getSubData(origin: Vec2i, size: Vec2i, format: Int = this.format) =
		getSubData(origin.x, origin.y, size.x, size.y, format)

	override fun setData(data: ByteBuffer, format: Int) {
		bind()
		if (data.lim != width * height * numChannels) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

		glTexImage2D(target, 0, format, width, height, 0, format, dataType, buf)
		MemoryUtil.memFree(buf)
	}

	fun setSubData(
		data: ByteBuffer,
		x: Int = 0,
		y: Int = 0,
		width: Int = this.width,
		height: Int = this.height,
		format: Int = this.format
	) {
		bind()
		if (data.lim != width * height * numChannels) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

		glTexSubImage2D(target, 0, x, y, width, height, format, dataType, buf)
		MemoryUtil.memFree(buf)
	}

	fun setSubData(data: ByteBuffer, origin: Vec2i, size: Vec2i, format: Int = this.format) =
		setSubData(data, origin.x, origin.y, size.x, size.y, format)

	override fun clear() {
//		val PBO = glGenBuffers()
//		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, PBO)
//		glBufferData(GL_PIXEL_UNPACK_BUFFER, bytes.toLong(), GL_STREAM_DRAW)
		bind()
		glTexImage2D(target, 0, internalFormat, width, height, 0, format, dataType, BufferUtils.createByteBuffer(bytes))
	}

	override fun setSamples(samples: Int, fixedSample: Boolean) {
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texturePointer)
		glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, format, width, height, fixedSample)
	}

	fun savePNG(file: String): Boolean {
		val d = getData(GL_UNSIGNED_BYTE, numChannels)
		d.limit(d.capacity())
		val fileName = if (file.endsWith(".png")) file else "$file.png"
		STBImageWrite.stbi_flip_vertically_on_write(true)
		return STBImageWrite.stbi_write_png(fileName, width, height, numChannels, d, numChannels * width)
	}

	fun savePNG(file: String, x: Int, y: Int, width: Int, height: Int, format: Int = this.format): Boolean {
		val d = getSubData(x, y, width, height)
		d.limit(d.capacity())
		val numChannels = TextureLoader.formatToChannels(format)
		return STBImageWrite.stbi_write_png(file, width, height, numChannels, d, numChannels * width)
	}

	/**
	 *  Returns a new Texture containing a cropped version of this
	 *
	 *  @param origin The pixel coordinate of the bottom left of the cropping
	 *  @param tr The exclusive pixel coordinate of the top right of the cropping
	 *
	 *  @returns A new texture of size ([tr] - [origin]), containing the pixels between [origin] and [tr] - (1, 1)
	 */
	fun crop(origin: Vec2i, tr: Vec2i): Texture{
		val size = tr - origin

		val buffer = MemoryUtil.memAlloc(size.x * size.y * numChannels)
		val texture = Texture("Cropping of $id", TextureLoader.createTexture(buffer, size.x, size.y, format))
		MemoryUtil.memFree(buffer)

		val copier = TextureCopyFramebuffer()
		copier.init()
		copier.setDst(texture)
		copier.setSrc(this)
		copier.copyTexture(origin, tr, Vec2i(0), size)
		copier.delete()
		return texture
	}

	override fun delete() {
		unbind()
		glDeleteTextures(texturePointer)
	}

	override fun toString(): String {
		return "Texture[$id]"
	}

	override fun equals(other: Any?): Boolean {
		if (other is Texture) return this.texturePointer == other.texturePointer
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
			val ptr = glGenTextures()

			// Settings
			glBindTexture(params.target, ptr)
			params.load()

			return ptr
		}

		fun create(id: String, width: Int, height: Int, format: Int, internalFormat: Int = format, params: TextureParameters = TextureParameters()): Texture {
			val ptr = createPointer(params)
			TextureLoader.writeTextureToPointer(null, width, height, format, internalFormat)
			return Texture(id, ptr)
		}

		val none: Texture = Texture("None", 0)
		val broke: Texture = Texture("missing", TextureLoader.createTexture(createArray().toBuffer(), 32, 32))

		fun createArray(): ByteArray {
			val b = byteArrayOf(0, 0, 0)
			val m = byteArrayOf(-1, 0, -1)
			val row = (m repeat 16) + (b repeat 16)
			val row2 = (b repeat 16) + (m repeat 16)
			return (row repeat 16) + (row2 repeat 16)
		}
	}
}