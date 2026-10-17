package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.fonts.FontLoader
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.shaders.ComputeShader
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.video.VideoLoader
import com.pineypiney.game_engine.util.extension_functions.resize
import com.pineypiney.game_engine.util.extension_functions.round
import kool.free
import org.lwjgl.system.MemoryUtil
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

class ResourcesLoader(val factory: ResourceFactory, val sources: List<Source>) {

	val shaderLocation = "shaders/"
	val textureLocation = "textures/"
	val fontLocation = "fonts/"
	val audioLocation = "audio/"
	val videoLocation = "videos/"
	val modelLocation = "models/"

	init {
		INSTANCE = this
	}

	fun getStream(name: String): InputStream? {
		for (source in sources) {
			if (source.streamList.contains(name)) return source.getStream(name)
		}
		return null
	}

	fun loadInitialResources() {
		RenderShader.initDefaultShader(this)
		ComputeShader.initDefaultShader(this)
		Mesh.initDefaultMeshes(factory)
		Texture2D.initDefaultTextures(factory)
		Texture3D.initDefaultTextures(factory)
		ModelMesh.initDefault(factory)
	}

	fun loadResources() {


		GameEngineI.info(
			"Loaded Shaders in ${
				timeActionM {
					Streams(this, "shaders/").useEachStream { fileName, stream ->
						ShaderLoader.INSTANCE.loadShaderModule(fileName, stream, this)
					}
				}.round(2)
			} ms", this
		)

		GameEngineI.info("Loaded Textures in ${timeActionM { loadTextures() }.round(2)} ms")
		GameEngineI.info("Loaded Audio in ${timeActionM { AudioLoader.INSTANCE.loadAudio(Streams(this, audioLocation)) }.round(2)} ms")
		GameEngineI.info("Loaded Models in ${timeActionM { loadModels() }.round(2)} ms")
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
		FontLoader.INSTANCE.delete()
	}

	companion object {

		lateinit var INSTANCE: ResourcesLoader; private set

		fun timeAction(action: () -> Unit): Long {
			val start = System.nanoTime()
			action()
			return System.nanoTime() - start
		}

		fun timeActionM(action: () -> Unit): Double {
			return timeAction(action) * 1e-6
		}

		fun ioResourceToByteBuffer(stream: InputStream, bufferSize: Int = stream.available(), resize: Boolean = true): ByteBuffer {

			val rbc: ReadableByteChannel = Channels.newChannel(stream)
			var buffer: ByteBuffer = MemoryUtil.memAlloc(bufferSize)

			while (true) {
				val bytes = rbc.read(buffer)
				if (stream.available() == 0 || bytes == -1 || !resize) {
					break
				}
				if (buffer.remaining() == 0) {
					val newBuffer = buffer.resize(buffer.capacity() * 2)
					buffer.free()
					buffer = newBuffer
				}
			}

			return buffer.flip()
		}

		fun directory(factory: ResourceFactory, file: File = File("src/main/resources")): ResourcesLoader {
			return ResourcesLoader(factory, listOf(DirectorySource(file)))
		}

		fun directoryAndEngine(factory: ResourceFactory, file: File = File("src/main/resources")): ResourcesLoader {
			return ResourcesLoader(factory, listOf(DirectorySource(file), GithubSource.ENGINE))
		}

		fun development(factory: ResourceFactory, engineRepo: String = "../GameEngine"): ResourcesLoader {
			return ResourcesLoader(factory, listOf(DirectorySource("src/main/resources"), DirectorySource("$engineRepo/src/main/resources")))
		}
	}


	class Streams(val loader: ResourcesLoader, val prefix: String, val extensions: Set<String>? = null) : Iterator<Pair<String, InputStream?>>, Iterable<Pair<String, InputStream?>> {
		val sources = loader.sources.map {
			it to it.streamList.filter { name ->
				name.startsWith(prefix) && extensions?.contains(name.substringAfterLast('.')) ?: true
			}
		}.filter { it.second.isNotEmpty() }
		var source = -1
		lateinit var allStreams: Iterator<String>

		fun setSourceStreams() {
			allStreams = sources[source].second.iterator()
		}

		override fun next(): Pair<String, InputStream?> {
			if (source == -1 || !allStreams.hasNext()) {
				source++
				setSourceStreams()
			}
			val name = allStreams.next()
			return name.removePrefix(prefix) to sources[source].first.getStream(name)
		}

		override fun hasNext(): Boolean = sources.isNotEmpty() && (source < sources.size - 1 || allStreams.hasNext())

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