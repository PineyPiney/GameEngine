package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.TextureCopyFramebuffer
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.repeat
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import kool.Buffer
import kool.lim
import kool.toBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45C.*
import org.lwjgl.stb.STBImageWrite
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class Texture3D(
	override val id: String,
	override val texturePointer: Int,
	override val target: Int = GL_TEXTURE_3D,
	override var binding: Int = 0
) : TextureI() {

	val size get() = Vec3i(width, height, depth)
//	val aspectRatio get() = width.f / height

	override fun setData(data: ByteBuffer, format: Int) {
		bind()
		if (data.lim != width * height * depth * numChannels) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
		glTexImage3D(target, 0, format, width, height, depth, 0, format, dataType, buf)

		MemoryUtil.memFree(buf)
	}

	fun getSliceData(layer: Int, buffer: ByteBuffer, type: Int = dataType) {
		glPixelStorei(GL_PACK_ALIGNMENT, 1)
		glGetTextureSubImage(texturePointer, 0, 0, 0, layer, width, height, 1, format, type, buffer)
	}

	fun getSubData(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int, format: Int = this.format): ByteBuffer {
		val buffer = BufferUtils.createByteBuffer(bytes)
		glPixelStorei(GL_PACK_ALIGNMENT, 1)
		glGetTextureSubImage(texturePointer, 0, x, y, z, width, height, depth, format, dataType, buffer)
		return buffer
	}

	fun getSubData(origin: Vec3i, size: Vec3i, format: Int = this.format) =
		getSubData(origin.x, origin.y, origin.z, size.x, size.y, size.z, format)

	fun setSubData(
		data: ByteBuffer,
		x: Int = 0,
		y: Int = 0,
		z: Int = 0,
		width: Int = this.width,
		height: Int = this.height,
		depth: Int = this.depth,
		format: Int = this.format
	) {
		bind()
		if (data.lim != width * height * depth * numChannels) {
			GameEngineI.warn("Buffer is not the right size to set texture data")
		}
		val buf = Buffer(data.lim) { data.get(it) }

		// https://stackoverflow.com/questions/9950546/c-opengl-glteximage2d-access-violation
		// Apparently OpenGL can randomly reset this value.
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
		glTexSubImage3D(target, 0, x, y, z, width, height, depth, format, dataType, buf)

		MemoryUtil.memFree(buf)
	}

	fun setSubData(data: ByteBuffer, origin: Vec3i, size: Vec3i, format: Int = this.format) =
		setSubData(data, origin.x, origin.y, origin.z, size.x, size.y, size.z, format)

	override fun clear() {
//		val PBO = glGenBuffers()
//		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, PBO)
//		glBufferData(GL_PIXEL_UNPACK_BUFFER, bytes.toLong(), GL_STREAM_DRAW)
		bind()
		glTexImage3D(target, 0, internalFormat, width, height, depth, 0, format, dataType, BufferUtils.createByteBuffer(bytes))
	}

	override fun setSamples(samples: Int, fixedSample: Boolean) {
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texturePointer)
		glTexImage3DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, format, width, height, depth, fixedSample)
	}

	fun saveStripPNG(file: String): Boolean {
		val d = getData(GL_UNSIGNED_BYTE, numChannels)
		d.limit(d.capacity())
		val fileName = if (file.endsWith(".png")) file else "$file.png"
		STBImageWrite.stbi_flip_vertically_on_write(true)
		return STBImageWrite.stbi_write_png(fileName, width, height * depth, numChannels, d, numChannels * width)
	}

	fun saveAtlasPNG(file: String, width: Int): Boolean {
		val height = Math.ceilDiv(depth, width)
		val atlas = Texture.create("$id Texture Atlas", this.width * width, this.height * height, format, internalFormat)
		val copier = TextureCopyFramebuffer()
		copier.init()
		copier.setDst(atlas)
		for (layer in 0 until depth) {
			copier.setSrc(this, layer)
			val x = (layer % width) * this.width
			val y = (layer / width) * this.height
			val o = Vec2i(x, y)
			copier.copyOntoDst(this, layer, o, o + Vec2i(size))
		}
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
		val texture = Texture3D("Cropping of $id", TextureLoader.createTexture3D(null, size.x, size.y, size.z, format))
		val copier = TextureCopyFramebuffer()
		copier.init()

		val cropOrigin = Vec2i(origin)
		val cropTR = Vec2i(tr)
		val cropSize = Vec2i(size)
		for (z in origin.z..<tr.z) {
			copier.setDst(texture, z)
			copier.setSrc(this, z)
			copier.copyTexture(cropOrigin, cropTR, Vec2i(0), cropSize)
		}
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
		if (other is Texture3D) return this.texturePointer == other.texturePointer
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

		fun create(id: String, width: Int, height: Int, format: Int, internalFormat: Int = format, params: TextureParameters = TextureParameters()): Texture3D {
			val ptr = createPointer(params)
			TextureLoader.writeTextureToPointer(null, width, height, format, internalFormat)
			return Texture3D(id, ptr)
		}

		val none: Texture3D = Texture3D("None", 0)
		val broke: Texture3D = Texture3D("missing", TextureLoader.createTexture(createArray().toBuffer(), 32, 32))

		fun createArray(): ByteArray {
			val b = byteArrayOf(0, 0, 0)
			val m = byteArrayOf(-1, 0, -1)
			val row = (m repeat 16) + (b repeat 16)
			val row2 = (b repeat 16) + (m repeat 16)
			return (row repeat 16) + (row2 repeat 16)
		}
	}
}