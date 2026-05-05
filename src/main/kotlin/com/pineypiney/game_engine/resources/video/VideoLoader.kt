package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.ResourceKey
import kool.cap
import org.bytedeco.ffmpeg.global.avutil.*
import org.bytedeco.javacv.FFmpegFrameGrabber
import java.nio.ByteBuffer
import java.nio.ShortBuffer

class VideoLoader private constructor() {

	val videos = mutableMapOf<ResourceKey, String>()

	fun delete() {

	}

	companion object {
		val INSTANCE: VideoLoader = VideoLoader()

		operator fun get(key: ResourceKey, loader: ResourcesLoader): Video {
			val location = INSTANCE.videos[key]
			val stream = loader.getStream("videos/$location")

			val grabber = FFmpegFrameGrabber(stream).apply { start() }
			val images = mutableListOf<ByteBuffer>()
			val audio = mutableListOf<ShortBuffer>()
			while (true) {
				val frame = grabber.grab() ?: break
				if (frame.image != null) {
					val buf = frame.image[0] as ByteBuffer
					val copy = ByteBuffer.allocate(buf.cap)
					copy.put(buf)
					images.add(copy)
				} else if (frame.samples != null) audio.add(frame.samples[0] as ShortBuffer)
			}
			val att = VideoAttributes(grabber)
			grabber.stop()
			grabber.release()

			return Video(key.key, loader.factory, att, images.toTypedArray(), audio.toTypedArray())
		}

		/*
		fun compileVideo(name: String, stream: InputStream): Video{
			val grabber = FFmpegFrameGrabber(stream)
			grabber.start()

			val length = grabber.lengthInTime.d / 1e6
			val frameRate = grabber.frameRate
			val frameCount = grabber.frameNumber

			val audioA = mutableListOf<Short>()
			val textures = mutableListOf<ByteBuffer>()

			// https://www.tabnine.com/code/java/classes/org.bytedeco.javacv.FFmpegFrameGrabber?snippet=5921ec434002b00004d6afd4

			val times = Vec3d()

			var i = 0
			while (true) {
				try {
					val t = System.nanoTime()
					val frame: Frame = grabber.grab() ?: break
					times.x += System.nanoTime() - t


					times.y += ResourcesLoader.timeAction {
						val frameBuffer: Buffer? = frame.image?.getOrNull(0)
						if(frameBuffer is ByteBuffer && textures.size < 10){
							textures.add(frameBuffer)
						}
					}

					times.z += ResourcesLoader.timeAction {
						val audioBuffer: Buffer? = frame.samples?.getOrNull(0)
						if(audioBuffer is ShortBuffer){
							audioA.addAll(audioBuffer.toList())
						}
					}
				}
				catch (e: Exception) {
					e.printStackTrace()
				}
			}

			println("Times are ${times / 1e9}")

			val b = grabber.audioChannels + grabber.audioBitrate
			val audioBuffer = audioA.flatMap { listOf((it and 0xff).b, (it shr 8).b) }.toByteArray().toBuffer()
			val audio = AudioLoader.loadAudio(audioBuffer, AL_FORMAT_STEREO16, grabber.sampleRate)

			val texture = Texture(name, TextureLoader.createAtlas(textures, grabber.imageWidth, grabber.imageHeight, TextureLoader.channelsToFormat(grabber.pixelFormat), GL20.GL_BGR))

			grabber.stop()
			grabber.release()

			return Video(texture, AudioSource(audio), length, frameRate, frameCount)
		}

		 */

		fun formatFromFfmpeg(ffmpeg: Int): TextureFormat {
			return when (ffmpeg) {
				AV_PIX_FMT_RGB24 -> TextureFormat.RGB8
				AV_PIX_FMT_BGR24 -> TextureFormat.BGR8
				AV_PIX_FMT_RGB8 -> TextureFormat.RGB332
				AV_PIX_FMT_BGR8 -> TextureFormat.BGR332
				AV_PIX_FMT_GRAY8 -> TextureFormat.R8
				AV_PIX_FMT_RGBA -> TextureFormat.RGBA8
				AV_PIX_FMT_BGRA -> TextureFormat.BGRA8
				else -> TextureFormat.RGB8
			}
		}
	}
}