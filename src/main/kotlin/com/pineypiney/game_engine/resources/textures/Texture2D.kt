package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.extension_functions.repeat
import glm_.f
import glm_.vec2.Vec2i
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

interface Texture2D : Texture {

	val size get() = Vec2i(width, height)
	val aspectRatio get() = width.f / height

	fun getSubData(x: Int, y: Int, width: Int, height: Int, format: TextureFormat = this.format): ByteBuffer

	fun getSubData(origin: Vec2i, size: Vec2i, format: TextureFormat = this.format) =
		getSubData(origin.x, origin.y, size.x, size.y, format)

	fun setSubData(data: ByteBuffer, x: Int = 0, y: Int = 0, width: Int = this.width, height: Int = this.height, format: TextureFormat = this.format)

	fun setSubData(data: ByteBuffer, origin: Vec2i, size: Vec2i, format: TextureFormat = this.format) =
		setSubData(data, origin.x, origin.y, size.x, size.y, format)

	fun savePNG(file: String, format: TextureFormat = this.format): Boolean {
		format.pixelType
		val d = getData(format)
		d.limit(d.capacity())
		val fileName = if (file.endsWith(".png")) file else "$file.png"
		STBImageWrite.stbi_flip_vertically_on_write(true)
		return STBImageWrite.stbi_write_png(fileName, width, height, format.pixelSize, d, format.pixelSize * width)
	}

	fun savePNG(file: String, x: Int, y: Int, width: Int, height: Int, format: TextureFormat = TextureFormat.RGBA8): Boolean {
		val d = getSubData(x, y, width, height, format)
		d.limit(d.capacity())
		// STBImage saves images in 8 bit channels, so the number of channels = the format's pixel size
		val numChannels = format.pixelSize
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
	fun crop(origin: Vec2i, tr: Vec2i): Texture2D {
		val size = tr - origin


		val texture = ResourceFactory.INSTANCE.createTexture2D("Cropping of $id", size.x, size.y, format, format, null, TextureParameters())

		val copier = createCopier()
		copier.init()
		copier.start()
		copier.setDst(texture)
		copier.setSrc(this)
		copier.copyTexture(origin, tr, Vec2i(0), size)
		copier.execute()
		copier.delete()
		return texture
	}

	companion object {

		fun create(id: String, width: Int, height: Int, internalFormat: TextureFormat, params: TextureParameters = TextureParameters()): Texture2D {
			return ResourceFactory.INSTANCE.createTexture2D(id, width, height, internalFormat, TextureFormat.RGBA8, null, params)
		}

		fun createErrorData(): ByteBuffer {
			val buffer = ByteBuffer.allocateDirect(4096)
			val b = byteArrayOf(0, 0, 0, -1) repeat 16
			val m = byteArrayOf(-1, 0, -1, -1) repeat 16
			repeat(16) {
				buffer.put(m)
				buffer.put(b)
			}
			repeat(16) {
				buffer.put(b)
				buffer.put(m)
			}
			return buffer.flip()
		}

		lateinit var none: Texture2D
		lateinit var missing: Texture2D

		fun initDefaultTextures(factory: ResourceFactory) {
			none = factory.nullTexture2D()
			missing = factory.createTexture2D("missing", 32, 32, TextureFormat.RGBA8, TextureFormat.RGBA8, createErrorData(), TextureParameters())
		}
	}
}