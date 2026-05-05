package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.DeletableResourceLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import com.pineypiney.game_engine.resources.textures.parameters.TextureFilter
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.resources.textures.parameters.TextureUsage
import com.pineypiney.game_engine.resources.textures.parameters.TextureWrap
import com.pineypiney.game_engine.util.Debug
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec3.Vec3i
import kool.toBuffer
import org.lwjgl.opengl.GL32C.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

class TextureLoader private constructor() : DeletableResourceLoader<Texture2D>() {

	override val missing: Texture2D get() = Texture2D.missing
	val flags = mutableMapOf<String, TextureParameters>()

	fun loadTextures(streams: ResourcesLoader.Streams) {

//		streams.useEachStream { _, stream ->
//			val buffer = ResourcesLoader.ioResourceToByteBuffer(stream)
//			val (data, vec) = loadTextureData(buffer, true, 0)
//			if(data != null) STBImage.stbi_image_free(data)
//			buffer.free()
//		}

		streams.useEachStream { fileName, stream ->
			val keyName = fileName.substringBefore('.')
			val params = flags[keyName] ?: flags["*"] ?: TextureParameters()
			val texture = streams.engine.resourcesLoader.factory.loadTexture2DFromFile(fileName, stream, params)
			map[ResourceKey(keyName)] = texture
		}
	}

	fun loadParameters(streams: ResourcesLoader.Streams) {
		streams.useEachStream { fileName, stream ->

			var currentEdit: TextureParameters? = null
			val dir = fileName.removeSuffix(".params")
			val reader = stream.bufferedReader(Charsets.UTF_8)
			reader.forEachLine { line ->
				if (line.startsWith('"')) {
					val name = dir + line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'))
					if (!flags.containsKey(name)) flags[name] = TextureParameters()
					currentEdit = flags[name] ?: return@forEachLine
				} else if (line[0].isWhitespace()) {
					val (param, value) = line.split(':').map(String::trim)

					when (param) {
						"target" -> currentEdit?.target = value.toIntOrNull() ?: 0
						"flip" -> currentEdit?.flip = value.toBoolean() || value.toIntOrNull() == 1
						"channels" -> currentEdit?.numChannels = value.toIntOrNull() ?: 0

						"wrap" -> currentEdit?.withWrapping(parse(value, TextureWrap.CLAMP_TO_EDGE))
						"wrapS" -> currentEdit?.wrapS = parse(value, TextureWrap.CLAMP_TO_EDGE)
						"wrapT" -> currentEdit?.wrapT = parse(value, TextureWrap.CLAMP_TO_EDGE)
						"wrapR" -> currentEdit?.wrapR = parse(value, TextureWrap.CLAMP_TO_EDGE)

						"filter" -> currentEdit?.withFilter(parse(value, TextureFilter.LINEAR))
						"minFilter" -> currentEdit?.minFilter = parse(value, TextureFilter.LINEAR)
						"magFilter" -> currentEdit?.magFilter = parse(value, TextureFilter.LINEAR)

						"usage" -> currentEdit?.usage = parse(value, TextureUsage.SAMPLER)
					}
				}
			}
		}
	}

	fun findTexture(name: String): Texture2D {
		val t = map[ResourceKey(name)] ?: map.entries.firstOrNull { (key, _) ->
			key.key.contains(name)
		}?.value
		return t ?: Texture2D.none
	}

	override fun delete() {
		super.delete()
		Texture2D.missing.delete()
		Texture3D.missing.delete()
	}

	companion object {
		val fileTypes = setOf("png", "jpg", "jpeg", "tga", "bmp", "hdr")
		val INSTANCE = TextureLoader()

		operator fun get(key: ResourceKey) = INSTANCE[key]
		fun getTexture(key: ResourceKey): Texture2D = INSTANCE[key]
		fun findTexture(name: String): Texture2D = INSTANCE.findTexture(name)

		fun loadTextureData(buffer: ByteBuffer, flip: Boolean = true, numChan: Int = 0): Pair<ByteBuffer?, Vec3i> {

			MemoryStack.stackPush().use { stack ->
				// Arrays are Java equivalent for pointers
				val pointers = stack.mallocInt(3)

				// Set the flip state of the image (default to true)
				STBImage.stbi_set_flip_vertically_on_load(flip)

				// Load texture data from file
				val d = Debug().start()
				val data: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, pointers, pointers.slice(1, 1), pointers.slice(2, 1), numChan)
				d.add()
				return data to Vec3i(pointers[0], pointers[1], if (numChan == 0) pointers[2] else numChan)
			}
		}

		fun createMultisampleTexture(
			data: ByteArray,
			width: Int,
			height: Int,
			samples: Int = 4,
			format: TextureFormat = TextureFormat.RGB8,
			internalFormat: Int = format.opengl,
			target: Int = GL_TEXTURE_2D_MULTISAMPLE,
			fixedSample: Boolean = true
		): Int {
			val pointer = glGenTextures()
			glBindTexture(target, pointer)
			glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, internalFormat, width, height, fixedSample)
			OpenGlTexture2D.writeTextureToPointer(data.toBuffer(), width, height, format, internalFormat)
			return pointer
		}

		fun createAtlas(
			textures: Iterable<ByteBuffer>,
			dataFormat: TextureFormat = TextureFormat.RGB8,
			width: Int, height: Int,
			internalFormat: Int = dataFormat.opengl,
		): Int {
			val pointer = OpenGlTexture2D.createPointer(null, dataFormat, width * textures.count(), height, internalFormat)
			textures.forEachIndexed { i, data ->
				glTexSubImage2D(GL_TEXTURE_2D, 0, width * i, 0, width, height, dataFormat.openglLayout, dataFormat.pixelType, data)
			}
			return pointer
		}

		fun formatToChannels(format: TextureFormat?) = when (format?.openglLayout) {
			GL_RED,
			GL_RED_INTEGER -> 1

			GL_RG,
			GL_RG_INTEGER -> 2

			GL_RGB,
			GL_BGR,
			GL_RGB_INTEGER,
			GL_BGR_INTEGER -> 3

			GL_RGBA,
			GL_BGRA,
			GL_RGBA_INTEGER,
			GL_BGRA_INTEGER -> 4

			else -> 3
		}

		/**
		 * Gets the texture format returned by STBImage#load according to the number of channels
		 */
		fun channelsToStbiFormat(channels: Int?) = when (channels) {
			4 -> TextureFormat.RGBA8
			3 -> TextureFormat.RGB8
			2 -> TextureFormat.RG8
			1 -> TextureFormat.R_DEFAULT
			else -> TextureFormat.RGB8
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

		inline fun <reified E : Enum<E>> parse(string: String, default: E): E {
			return try {
				enumValueOf<E>(string.uppercase())
			} catch (e: Error) {
				default
			}
		}
	}
}