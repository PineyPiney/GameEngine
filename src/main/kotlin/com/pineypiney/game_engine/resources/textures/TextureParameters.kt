package com.pineypiney.game_engine.resources.textures

import org.lwjgl.opengl.GL11C.GL_LINEAR
import org.lwjgl.opengl.GL11C.GL_LINEAR_MIPMAP_LINEAR
import org.lwjgl.opengl.GL11C.GL_LINEAR_MIPMAP_NEAREST
import org.lwjgl.opengl.GL11C.GL_NEAREST
import org.lwjgl.opengl.GL11C.GL_NEAREST_MIPMAP_LINEAR
import org.lwjgl.opengl.GL11C.GL_NEAREST_MIPMAP_NEAREST
import org.lwjgl.opengl.GL11C.GL_REPEAT
import org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL13C.GL_CLAMP_TO_BORDER
import org.lwjgl.opengl.GL14C.GL_MIRRORED_REPEAT
import org.lwjgl.opengl.GL44C.GL_MIRROR_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL46C.*

data class TextureParameters(
	var target: Int = GL_TEXTURE_2D, var flip: Boolean = true, var numChannels: Int = 0,
	var wrapS: Int = GL_CLAMP_TO_EDGE, var wrapT: Int = wrapS, var wrapR: Int = wrapS,
	var minFilter: Int = GL_LINEAR, var magFilter: Int = minFilter
) {

	fun target(target: Int): TextureParameters {
		this.target = target
		return this
	}

	/**
	 *  @param [wrapping] Set the wrapping of the texture in all 3 directions. One of [GL_REPEAT], [GL_MIRRORED_REPEAT], [GL_CLAMP_TO_EDGE], [GL_CLAMP_TO_BORDER], [GL_MIRROR_CLAMP_TO_EDGE]
	 */
	fun withWrapping(wrapping: Int): TextureParameters {
		wrapS = wrapping
		wrapT = wrapping
		wrapR = wrapping
		return this
	}

	/**
	 *  @param [filter] Set the min and mag filter of the texture. One of [GL_NEAREST], [GL_LINEAR], [GL_NEAREST_MIPMAP_NEAREST], [GL_LINEAR_MIPMAP_NEAREST], [GL_NEAREST_MIPMAP_LINEAR], [GL_LINEAR_MIPMAP_LINEAR]
	 */
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

	/*

	Example File, which makes all textures in the directory have min and mag filters set to GL_NEAREST
	"*"
		filter : NEAREST

	 */
}
