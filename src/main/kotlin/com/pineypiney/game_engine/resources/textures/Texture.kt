package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.TextureCopyFrameBuffer
import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.repeat
import glm_.f
import glm_.vec2.Vec2i
import kool.Buffer
import kool.ByteBuffer
import kool.lim
import kool.toBuffer
import org.lwjgl.opengl.GL45C.*
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

class Texture(
	val id: String,
	val texturePointer: Int,
	val target: Int = GL_TEXTURE_2D,
	var binding: Int = 0
) : Resource() {

	val width: Int get() = parameter(GL_TEXTURE_WIDTH)
	val height: Int get() = parameter(GL_TEXTURE_HEIGHT)
	val internalFormat: Int get() = parameter(GL_TEXTURE_INTERNAL_FORMAT)
	val format: Int get() = TextureLoader.internalFormatToFormat(internalFormat)
	val numChannels: Int get() = TextureLoader.formatToChannels(format)
	val bytes: Int get() = width * height * TextureLoader.internalFormatToPixelSize(internalFormat)
	val dataType: Int get() = TextureLoader.internalFormatToDataType(internalFormat)

	val size get() = Vec2i(width, height)
	val aspectRatio get() = width.f / height


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
		val buffer = ByteBuffer(width * height * pixelSize)
		glFinish()
		glGetTexImage(target, 0, format, type, buffer)
		return buffer
	}

	fun getSubData(x: Int, y: Int, width: Int, height: Int, format: Int = this.format): ByteBuffer {
		val buffer = ByteBuffer(bytes)
		glPixelStorei(GL_PACK_ALIGNMENT, 1)
		glGetTextureSubImage(texturePointer, 0, x, y, 0, width, height, 1, format, dataType, buffer)
		return buffer
	}

	fun getSubData(origin: Vec2i, size: Vec2i, format: Int = this.format) =
		getSubData(origin.x, origin.y, size.x, size.y, format)

	fun setData(data: ByteBuffer, format: Int = this.format) {
		bind()
		if (data.lim != width * height * numChannels) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
		glTexImage2D(target, 0, format, width, height, 0, format, dataType, buf)
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
	}

	fun setSubData(data: ByteBuffer, origin: Vec2i, size: Vec2i, format: Int = this.format) =
		setSubData(data, origin.x, origin.y, size.x, size.y, format)

	fun clear(){
//		val PBO = glGenBuffers()
//		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, PBO)
//		glBufferData(GL_PIXEL_UNPACK_BUFFER, bytes.toLong(), GL_STREAM_DRAW)
		bind()
		glTexImage2D(target, 0, internalFormat, width, height, 0, format, dataType, ByteBuffer(bytes))
	}

	fun setSamples(samples: Int, fixedSample: Boolean = true) {
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texturePointer)
		glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, format, width, height, fixedSample)
	}

	fun parameter(param: Int): Int {
		return if (GLFunc.isLoaded) {
			bind()
			glGetTexLevelParameteri(target, 0, param)
		} else 0
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
		val texture = Texture("Cropping of $id", TextureLoader.createTexture(Buffer(size.x * size.y * numChannels), size.x, size.y, format))
		val copier = TextureCopyFrameBuffer()
		copier.init()
		copier.setDst(texture)
		copier.setSrc(this)
		copier.copyTexture(origin, tr)
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

		fun createPointer(params: TextureParameters = TextureParameters.default): Int {
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

		fun create(id: String, width: Int, height: Int, format: Int, internalFormat: Int = format, params: TextureParameters = TextureParameters.default): Texture{
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