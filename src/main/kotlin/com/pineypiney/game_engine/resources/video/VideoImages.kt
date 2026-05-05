package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import java.nio.ByteBuffer

class VideoImages(override val video: Video) : VideoData<Texture2D>() {

	val format = VideoLoader.formatFromFfmpeg(video.pixelFormat)

	val textures = Array(4) { video.factory.createTexture2D(video.name, video.width, video.height, format, TextureFormat.R8, null, TextureParameters()) }

	val current: Texture2D get() = textures[currentIndex % textures.size]

	override fun init() {
		nextUpdate = 0
		for (i in textures.indices) {
			val buffer = video.image(i)
			loadNextBuffer(buffer)
		}
	}

	override fun loadNextBuffer(buffer: ByteBuffer) {
		val index = nextUpdate % textures.size
		val updateTexture = textures[index]
		updateTexture.setData(buffer, format = format)
		nextUpdate++
	}

	override fun update() {
		val time = video.timeStamp

		while (nextUpdate * video.frameTime <= time) {
			updateNextFrame()
		}
	}

	fun updateNextFrame() {
		val t1 = Timer.getCurrentTime()
		val frame = video.image((nextUpdate + 4) % video.images.size)
		val t2 = Timer.getCurrentTime()

		loadNextBuffer(frame)
		val d1 = t2 - t1
		val d2 = Timer.getCurrentTime() - t2
		GameEngineI.debug("Stage times are $d1 and $d2")
		GameEngineI.debug("Frame Time is ${(nextUpdate + 4) * video.frameRate}")
	}

	override fun delete() {
		textures.deleteArray()
	}
}