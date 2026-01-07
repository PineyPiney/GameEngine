package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.video.VideoLoader
import com.pineypiney.game_engine.util.extension_functions.round
import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

abstract class ResourcesLoader(val location: String) {

	open val shaderLocation = "shaders/"
	open val textureLocation = "textures/"
	open val fontLocation = "fonts/"
	open val audioLocation = "audio/"
	open val videoLocation = "videos/"
	open val modelLocation = "models/"

	abstract val streamList: Set<String>

	abstract fun getStream(name: String): InputStream?

	fun loadResources() {
		GameEngineI.info("Loaded Shaders in ${timeActionM { ShaderLoader.INSTANCE.loadShaders(Streams(this, "shaders/")) }.round(2)} ms", this)
		GameEngineI.info("Loaded Textures in ${timeActionM(::loadTextures).round(2)} ms")
		GameEngineI.info("Loaded Audio in ${timeActionM { AudioLoader.INSTANCE.loadAudio(Streams(this, audioLocation)) }.round(2)} ms")
		GameEngineI.info("Loaded Models in ${timeActionM(::loadModels).round(2)} ms")
		GameEngineI.info("Loaded Fonts in ${timeActionM { FontLoader.INSTANCE.loadFonts(Streams(this, fontLocation)) }.round(2)} ms")
	}

	fun loadTextures() {
		TextureLoader.INSTANCE.loadParameters(Streams(this, textureLocation, setOf("params")))
		TextureLoader.INSTANCE.loadTextures(Streams(this, textureLocation, TextureLoader.fileTypes))
	}

	fun loadModels() {
		ModelLoader.INSTANCE.loadModelTextures(Streams(this, modelLocation, TextureLoader.fileTypes))
		ModelLoader.INSTANCE.loadModels(Streams(this, modelLocation))
	}

	fun cleanUp() {
		ShaderLoader.INSTANCE.delete()
		TextureLoader.INSTANCE.delete()
		AudioLoader.INSTANCE.delete()
		VideoLoader.INSTANCE.delete()
		ModelLoader.INSTANCE.delete()
	}

	companion object {

		fun lowercaseExtension(file: String): String = file.split('.').run { this[0] + '.' + this[1].lowercase() }

		fun timeAction(action: () -> Unit): Long {
			val start = System.nanoTime()
			action()
			return System.nanoTime() - start
		}

		fun timeActionM(action: () -> Unit): Double {
			return timeAction(action) * 1e-6
		}

		fun ioResourceToByteBuffer(stream: InputStream, bufferSize: Int = 1024, resize: Boolean = true): ByteBuffer {

			val rbc: ReadableByteChannel = Channels.newChannel(stream)
			var buffer: ByteBuffer = BufferUtils.createByteBuffer(bufferSize)

			while (true) {
				val bytes = rbc.read(buffer)
				if (bytes == -1 || !resize) {
					break
				}
				if (buffer.remaining() == 0) {
					buffer = resizeBuffer(buffer, buffer.capacity() * 2)
				}
			}

			buffer.flip()
			return buffer
		}

		private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
			val newBuffer = BufferUtils.createByteBuffer(newCapacity)
			buffer.flip()
			newBuffer.put(buffer)
			return newBuffer
		}
	}



	class Streams(val loader: ResourcesLoader, val prefix: String, val extensions: Set<String>? = null): Iterator<Pair<String, InputStream?>>, Iterable<Pair<String, InputStream?>> {

		val size: Int
		val allStreams: Iterator<String>
		init {
			val list = loader.streamList.filter { it.startsWith(prefix) }.filter {
				extensions?.contains(it.substringAfterLast('.')) ?: true
			}
			size = list.size
			allStreams = list.iterator()
		}

		override fun next(): Pair<String, InputStream?> {
			val name = allStreams.next()
			return name.removePrefix(prefix) to loader.getStream(name)
		}

		override fun hasNext(): Boolean = allStreams.hasNext()

		override fun iterator(): Iterator<Pair<String, InputStream?>> = this

		fun useEachStream(action: (String, InputStream) -> Unit) {
			for((name, stream) in this){
				if(stream == null) continue
				action(name, stream)
				stream.close()
			}
		}
	}
}