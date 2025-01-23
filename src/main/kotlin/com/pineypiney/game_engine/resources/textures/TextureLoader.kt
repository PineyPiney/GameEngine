package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.DeletableResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.Debug
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import glm_.bool
import glm_.vec3.Vec3i
import kool.toBuffer
import org.lwjgl.opengl.GL32C.*
import org.lwjgl.opengl.GL44.GL_MIRROR_CLAMP_TO_EDGE
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.ByteBuffer

class TextureLoader private constructor() : DeletableResourcesLoader<Texture>() {

	override val missing: Texture get() = Texture.broke
	val flags = mutableMapOf<String, TextureParameters>()

	fun loadTextures(streams: Map<String, InputStream>) {
		val pointers = IntArray(streams.size)
		glGenTextures(pointers)
		val pList = pointers.toMutableSet()

		val d = Debug()
		for ((fileName, stream) in streams) {
			d.start()
			val p = pList.firstOrNull() ?: glGenTextures()
			val keyName = fileName.substringBefore('.')
			val params = flags[keyName] ?: flags["*"] ?: TextureParameters.default

			loadIndividualSettings(p, params)
			val np = loadTextureFromStream(fileName, stream, params, p)
			val texture = Texture(fileName, np)
			map[ResourceKey(keyName)] = texture

			pList.remove(p)
			stream.close()

			d.add()
		}
	}

	fun loadParameters(streams: Map<String, InputStream>) {
		for ((fileName, stream) in streams) {
			var currentEdit: TextureParameters? = null
			val dir = fileName.removeSuffix(".params")
			val lines = stream.readBytes().toString(Charsets.UTF_8).split('\n')

			for (line in lines) {
				if (line.startsWith('"')) {
					val name = dir + line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'))
					if (!flags.containsKey(name)) flags[name] = TextureParameters()
					currentEdit = flags[name] ?: return
				} else if (line[0].isWhitespace()) {
					val (param, value) = line.split(':').map(String::trim)
					val intValue = parameters.getOrElse(value) {
						try {
							Integer.parseInt(value)
						} catch (e: NumberFormatException) {
							GameEngineI.warn("Couldn't parse texture parameter $param with value $value in .params file $fileName")
							0
						}
					}
					when (param) {
						"target" -> currentEdit?.target = intValue
						"flip" -> currentEdit?.flip = intValue.bool
						"channels" -> currentEdit?.numChannels = intValue
						"wrap" -> currentEdit?.setWrapping(intValue)
						"wrapS" -> currentEdit?.wrapS = intValue
						"wrapT" -> currentEdit?.wrapT = intValue
						"wrapR" -> currentEdit?.wrapR = intValue
						"filter" -> currentEdit?.setFilter(intValue)
						"minFilter" -> currentEdit?.minFilter = intValue
						"magFilter" -> currentEdit?.magFilter = intValue
					}
				}
			}
		}
	}

	fun findTexture(name: String): Texture {
		val t = map[ResourceKey(name)] ?: map.entries.firstOrNull { (key, _) ->
			key.key.contains(name)
		}?.value
		return t ?: Texture.broke
	}

	companion object {
		val fileTypes = arrayOf("png", "jpg", "jpeg", "tga", "bmp", "hdr")
		val INSTANCE = TextureLoader()

		operator fun get(key: ResourceKey) = INSTANCE[key]
		fun getTexture(key: ResourceKey): Texture = INSTANCE[key]
		fun findTexture(name: String): Texture = INSTANCE.findTexture(name)


		fun createPointer(params: TextureParameters = TextureParameters.default): Int {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not create texture pointer because OpenGL has not been loaded")
				return -1
			}
			// Create a handle for the texture
			val texturePointer = glGenTextures()
			// Settings
			loadIndividualSettings(texturePointer, params)

			return texturePointer
		}

		fun loadIndividualSettings(pointer: Int, params: TextureParameters = TextureParameters.default) {

			glBindTexture(params.target, pointer)
			// Set wrapping and filtering options
			glTexParameteri(params.target, GL_TEXTURE_WRAP_S, params.wrapS)
			glTexParameteri(params.target, GL_TEXTURE_WRAP_T, params.wrapT)
			glTexParameteri(params.target, GL_TEXTURE_WRAP_R, params.wrapR)
			glTexParameteri(params.target, GL_TEXTURE_MIN_FILTER, params.minFilter)
			glTexParameteri(params.target, GL_TEXTURE_MAG_FILTER, params.magFilter)
		}

		fun loadTextureFromStream(
			name: String,
			texture: InputStream,
			params: TextureParameters = TextureParameters.default,
			pointer: Int = createPointer(params)
		): Int {
			val bb = ResourcesLoader.ioResourceToByteBuffer(texture, 1024)
			val p = loadTextureFromBuffer(name, bb, params, pointer)
			return p
		}

		private fun loadTextureFromBuffer(
			name: String,
			buffer: ByteBuffer,
			params: TextureParameters = TextureParameters.default,
			pointer: Int = createPointer(params)
		): Int {
			if (!buffer.hasRemaining()) {
				GameEngineI.warn("Buffer for texture $name is empty")
			}
			val (data, vec) = loadTextureData(buffer, params.flip, params.numChannels)
			if (data != null) {

				val format = channelsToFormat(vec.z)
				writeTextureToPointer(data, vec.x, vec.y, format, format, GL_UNSIGNED_BYTE)

				STBImage.stbi_image_free(data)
				return pointer
			}

			GameEngineI.warn("\nSTB failed to load texture $name")
			buffer.clear()
			return 0
		}

		fun loadTextureData(buffer: ByteBuffer, flip: Boolean = true, numChan: Int = 0): Pair<ByteBuffer?, Vec3i> {
			// Arrays are Java equivalent for pointers
			val widthA = IntArray(1)
			val heightA = IntArray(1)
			val numChannelsA = IntArray(1)

			// Set the flip state of the image (default to true)
			STBImage.stbi_set_flip_vertically_on_load(flip)

			// Load texture data from file
			val d = Debug().start()
			val data: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, widthA, heightA, numChannelsA, numChan)
			d.add()
			return data to Vec3i(widthA[0], heightA[0], if (numChan == 0) numChannelsA[0] else numChan)
		}

		fun writeTextureToPointer(
			data: ByteBuffer?,
			width: Int,
			height: Int,
			format: Int,
			internalFormat: Int,
			type: Int,
			debug: Boolean = false
		) {
			if (!GLFunc.isLoaded) {
				GameEngineI.warn("Could not write texture to pointer because OpenGL has not been loaded")
				return
			}
			val d = Debug().start()
			if(data == null) GameEngineI.warn("write null data to texture")
			if(debug) GameEngineI.debug("Calling texImage2D with internalFormat: $internalFormat, width: $width, height: $height, format: $format, type: $type and data: $data")
			try { glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, data) }
			catch (e: Exception){ "TEXTURE FAILURE" }

			d.add()

			glGenerateMipmap(GL_TEXTURE_2D)
		}

		fun createTexture(
			data: ByteBuffer?,
			width: Int,
			height: Int,
			format: Int = GL_RGB,
			internalFormat: Int = format,
			type: Int = GL_UNSIGNED_BYTE,
			params: TextureParameters = TextureParameters.default,
			debug: Boolean = false
		): Int {
			val pointer = createPointer(params)
			if(debug) GameEngineI.debug("Calling writeTextureToPointer on texturePtr: $pointer with parameters: $params")
			if (pointer != -1) writeTextureToPointer(data, width, height, format, internalFormat, type, debug)
			return pointer
		}

		fun createMultisampleTexture(
			data: ByteArray,
			width: Int,
			height: Int,
			samples: Int = 4,
			format: Int = GL_RGB,
			internalFormat: Int = format,
			type: Int = GL_UNSIGNED_BYTE,
			target: Int = GL_TEXTURE_2D_MULTISAMPLE,
			fixedSample: Boolean = true
		): Int {
			val pointer = glGenTextures()
			glBindTexture(target, pointer)
			glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, format, width, height, fixedSample)
			writeTextureToPointer(data.toBuffer(), width, height, format, internalFormat, type)
			return pointer
		}

		fun createAtlas(
			textures: Iterable<ByteBuffer>,
			width: Int,
			height: Int,
			format: Int = GL_RGB,
			internalFormat: Int = format,
			type: Int = GL_UNSIGNED_BYTE
		): Int {
			val pointer = createTexture(null, width * textures.count(), height, format, internalFormat)
			textures.forEachIndexed { i, data ->
				glTexSubImage2D(GL_TEXTURE_2D, 0, width * i, 0, width, height, format, type, data)
			}
			return pointer
		}

		fun formatToChannels(format: Int?) = when (format) {
			GL_RGB,
			GL_BGR -> 3

			GL_RGBA,
			GL_BGRA -> 4

			GL_RED,
			GL_GREEN,
			GL_BLUE,
			GL_ALPHA -> 1

			else -> 3
		}

		fun channelsToFormat(channels: Int?) = when (channels) {
			4 -> GL_RGBA
			3 -> GL_RGB
			1 -> GL_RED
			else -> GL_RGB
		}

		fun setFlags(name: String, flip: Boolean, numChannels: Int = 0) {
			INSTANCE.flags[name] = TextureParameters(flip = flip, numChannels = numChannels)
		}

		fun setFlags(name: String, numChan: Int) {
			INSTANCE.flags[name] = TextureParameters(numChannels = numChan)
		}

		fun setFlags(name: String, params: TextureParameters) {
			INSTANCE.flags[name] = params
		}

		val parameters = mapOf(
			Pair("NEAREST", GL_NEAREST),
			Pair("LINEAR", GL_LINEAR),
			Pair("NEAREST_NEAREST", GL_NEAREST_MIPMAP_NEAREST),
			Pair("LINEAR_NEAREST", GL_LINEAR_MIPMAP_NEAREST),
			Pair("NEAREST_LINEAR", GL_NEAREST_MIPMAP_LINEAR),
			Pair("LINEAR_LINEAR", GL_LINEAR_MIPMAP_LINEAR),
			Pair("RED", GL_RED),
			Pair("GREEN", GL_GREEN),
			Pair("BLUE", GL_BLUE),
			Pair("ALPHA", GL_ALPHA),
			Pair("ZERO", GL_ZERO),
			Pair("ONE", GL_ONE),
			Pair("CLAMP_TO_EDGE", GL_CLAMP_TO_EDGE),
			Pair("CLAMP_TO_BORDER", GL_CLAMP_TO_BORDER),
			Pair("MIRRORED_REPEAT", GL_MIRRORED_REPEAT),
			Pair("REPEAT", GL_REPEAT),
			Pair("MIRROR_CLAMP_TO_EDGE", GL_MIRROR_CLAMP_TO_EDGE),
		)
	}
}