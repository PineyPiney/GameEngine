package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

interface Texture3D : Texture {

	val size get() = Vec3i(width, height, depth)

	fun getSubData(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, format: TextureFormat = this.format): ByteBuffer

	fun getSubData(origin: Vec3i, size: Vec3i, format: TextureFormat = this.format) =
		getSubData(origin.x, origin.y, origin.z, size.x, size.y, size.z, format)

	fun setSubData(data: ByteBuffer, x: Int = 0, y: Int = 0, z: Int = 0, width: Int, height: Int, depth: Int, format: TextureFormat = this.format)

	fun setSubData(data: ByteBuffer, origin: Vec3i, size: Vec3i, format: TextureFormat = this.format) =
		setSubData(data, origin.x, origin.y, origin.z, size.x, size.y, size.z, format)

	fun getSliceData(layer: Int, format: TextureFormat = this.format): ByteBuffer {
		return getSubData(0, 0, layer, width, height, 1, format)
	}

	fun saveStripPNG(file: String): Boolean {
		val d = getData()
		d.limit(d.capacity())
		val fileName = if (file.endsWith(".png")) file else "$file.png"
		STBImageWrite.stbi_flip_vertically_on_write(true)
		return STBImageWrite.stbi_write_png(fileName, width, height * depth, numChannels, d, numChannels * width)
	}

	fun saveAtlasPNG(file: String, width: Int): Boolean {
		val height = Math.ceilDiv(depth, width)
		val atlas = Texture2D.create("$id Texture Atlas", this.width * width, this.height * height, format)
		val copier = createCopier()
		copier.init()
		copier.start()
		copier.setDst(atlas)
		for (layer in 0 until depth) {
			copier.setSrc(this, layer)
			val x = (layer % width) * this.width
			val y = (layer / width) * this.height
			val o = Vec2i(x, y)
			copier.copyOntoDst(this, layer, o, o + Vec2i(size))
		}
		copier.execute()
		copier.delete()
		return atlas.savePNG(file)
	}

	/**
	 *  Returns a new Texture3D containing a cropped version of this
	 *
	 *  @param origin The pixel coordinate of the bottom left of the cropping
	 *  @param tr The exclusive pixel coordinate of the top right of the cropping
	 *
	 *  @returns A new texture of size ([tr] - [origin]), containing the pixels between [origin] and [tr] - (1, 1)
	 */
	fun crop(origin: Vec3i, tr: Vec3i): Texture3D {
		val size = tr - origin
		val texture = ResourceFactory.INSTANCE.createTexture3D("Cropping of $id", size.x, size.y, size.z, format, format, null, TextureParameters())

		val copier = createCopier()
		copier.init()
		copier.start()
		val cropOrigin = Vec2i(origin)
		val cropTR = Vec2i(tr)
		val cropSize = Vec2i(size)
		for (z in origin.z..<tr.z) {
			copier.setDst(texture, z)
			copier.setSrc(this, z)
			copier.copyTexture(cropOrigin, cropTR, Vec2i(0), cropSize)
		}
		copier.execute()
		copier.delete()
		return texture
	}

	companion object {

		fun create(id: String, width: Int, height: Int, depth: Int, format: TextureFormat, internalFormat: TextureFormat = format, params: TextureParameters = TextureParameters()): Texture3D {
			return ResourceFactory.INSTANCE.createTexture3D(id, width, height, depth, format, internalFormat, null, params)
		}

		lateinit var none: Texture3D
		lateinit var missing: Texture3D

		fun initDefaultTextures(factory: ResourceFactory) {
			none = factory.nullTexture3D()
			missing = factory.createTexture3D("missing", 32, 32, 1, TextureFormat.RGB8, TextureFormat.RGBA8, Texture2D.createErrorData(), TextureParameters())
		}
	}
}