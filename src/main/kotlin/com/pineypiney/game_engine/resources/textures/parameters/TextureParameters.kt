package com.pineypiney.game_engine.resources.textures.parameters

import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL12C

data class TextureParameters(
	var target: Int = GL11C.GL_TEXTURE_2D, var flip: Boolean = true, var numChannels: Int = 0,
	var wrapS: TextureWrap = TextureWrap.CLAMP_TO_EDGE, var wrapT: TextureWrap = wrapS, var wrapR: TextureWrap = wrapS,
	var minFilter: TextureFilter = TextureFilter.LINEAR, var magFilter: TextureFilter = minFilter, var usage: TextureUsage = TextureUsage.SAMPLER
) {

	fun target(target: Int): TextureParameters {
		this.target = target
		return this
	}

	/**
	 *  @param [wrapping] Set the wrapping of the texture in all 3 directions. One of [GL11C.GL_REPEAT], [org.lwjgl.opengl.GL14C.GL_MIRRORED_REPEAT], [GL12C.GL_CLAMP_TO_EDGE], [org.lwjgl.opengl.GL13C.GL_CLAMP_TO_BORDER], [org.lwjgl.opengl.GL44C.GL_MIRROR_CLAMP_TO_EDGE]
	 */
	fun withWrapping(wrapping: TextureWrap): TextureParameters {
		wrapS = wrapping
		wrapT = wrapping
		wrapR = wrapping
		return this
	}

	/**
	 *  @param [filter] Set the min and mag filter of the texture. One of [GL11C.GL_NEAREST], [GL11C.GL_LINEAR], [GL11C.GL_NEAREST_MIPMAP_NEAREST], [GL11C.GL_LINEAR_MIPMAP_NEAREST], [GL11C.GL_NEAREST_MIPMAP_LINEAR], [GL11C.GL_LINEAR_MIPMAP_LINEAR]
	 */
	fun withFilter(filter: TextureFilter): TextureParameters {
		minFilter = filter
		magFilter = filter
		return this
	}

	fun usage(usage: TextureUsage): TextureParameters {
		this.usage = usage
		return this
	}

	fun loadOpenGL() {
		GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_WRAP_S, wrapS.opengl)
		GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_WRAP_T, wrapT.opengl)
		GL11C.glTexParameteri(target, GL12C.GL_TEXTURE_WRAP_R, wrapR.opengl)
		GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_MIN_FILTER, minFilter.opengl)
		GL11C.glTexParameteri(target, GL11C.GL_TEXTURE_MAG_FILTER, magFilter.opengl)
	}

	/*

	Example File, which makes all textures in the directory have min and mag filters set to GL_NEAREST
	"*"
		filter : NEAREST

	 */
}