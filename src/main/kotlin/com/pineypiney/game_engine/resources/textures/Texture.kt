package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.rendering.TextureCopier
import com.pineypiney.game_engine.resources.Resource
import java.nio.ByteBuffer

interface Texture : Resource {

	val id: String

	val width: Int
	val height: Int
	val depth: Int

	val format: TextureFormat
	val numChannels: Int get() = TextureLoader.formatToChannels(format)
	val bytes: Int get() = width * height * format.pixelSize

	fun getData(format: TextureFormat = this.format): ByteBuffer
	fun setData(data: ByteBuffer, format: TextureFormat = this.format)

	fun clear()

	fun setSamples(samples: Int, fixedSample: Boolean = true)

	fun createCopier(): TextureCopier
}