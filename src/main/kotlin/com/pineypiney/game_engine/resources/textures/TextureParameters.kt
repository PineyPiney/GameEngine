package com.pineypiney.game_engine.resources.textures

import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL30C

data class TextureParameters(
	var target: Int = GL_TEXTURE_2D, var flip: Boolean = true, var numChannels: Int = 0,
	var wrapS: Int = GL30C.GL_CLAMP_TO_EDGE, var wrapT: Int = wrapS, var wrapR: Int = wrapS,
	var minFilter: Int = GL30C.GL_LINEAR, var magFilter: Int = minFilter
) {
	fun setWrapping(wrapping: Int) {
		wrapS = wrapping
		wrapT = wrapping
		wrapR = wrapping
	}

	fun setFilter(filter: Int) {
		minFilter = filter
		magFilter = filter
	}

	companion object {
		val default = TextureParameters()
	}
}
