package com.pineypiney.game_engine.resources.textures

import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R
import org.lwjgl.opengl.GL30C

data class TextureParameters(
	var target: Int = GL_TEXTURE_2D, var flip: Boolean = true, var numChannels: Int = 0,
	var wrapS: Int = GL30C.GL_CLAMP_TO_EDGE, var wrapT: Int = wrapS, var wrapR: Int = wrapS,
	var minFilter: Int = GL_LINEAR, var magFilter: Int = minFilter
) {
	fun withWrapping(wrapping: Int): TextureParameters {
		wrapS = wrapping
		wrapT = wrapping
		wrapR = wrapping
		return this
	}

	fun withFilter(filter: Int): TextureParameters {
		minFilter = filter
		magFilter = filter
		return this
	}

	fun load(){
		glTexParameteri(target, GL_TEXTURE_WRAP_S, wrapS)
		glTexParameteri(target, GL_TEXTURE_WRAP_T, wrapT)
		glTexParameteri(target, GL_TEXTURE_WRAP_R, wrapR)
		glTexParameteri(target, GL_TEXTURE_MIN_FILTER, minFilter)
		glTexParameteri(target, GL_TEXTURE_MAG_FILTER, magFilter)
	}

	companion object {
		val default = TextureParameters()
	}

	/*

	Example File, which makes all textures in the directory have min and mag filters set to GL_NEAREST
	"*"
		filter : NEAREST

	 */
}
