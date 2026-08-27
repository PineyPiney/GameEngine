package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureAspect
import com.pineypiney.game_engine.resources.textures.parameters.TextureFilter
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

abstract class TextureCopier : Initialisable {

	var srcSize: Vec3i = Vec3i()
	var dstSize = Vec3i()

	abstract fun start()

	abstract fun setSrc(src: Texture2D)
	abstract fun setSrc(src: Texture3D, layer: Int)

	abstract fun setDst(dst: Texture2D)
	abstract fun setDst(dst: Texture3D, layer: Int)

	abstract fun copyTexture(
		srcOrigin: Vec2i = Vec2i(0),
		srcTR: Vec2i = Vec2i(srcSize),
		dstOrigin: Vec2i = Vec2i(0),
		dstTR: Vec2i = Vec2i(dstSize),
		mask: Collection<TextureAspect> = setOf(TextureAspect.COLOUR),
		filter: TextureFilter = TextureFilter.LINEAR
	)

	abstract fun copyTexture(
		srcOrigin: Vec3i = Vec3i(0),
		srcTR: Vec3i = srcSize,
		dstOrigin: Vec3i = Vec3i(0),
		dstTR: Vec3i = dstSize,
		mask: Collection<TextureAspect> = setOf(TextureAspect.COLOUR),
		filter: TextureFilter = TextureFilter.LINEAR
	)

	fun copyOntoDst(src: Texture2D, dstOrigin: Vec2i = Vec2i(0), dstTR: Vec2i = dstOrigin + src.size) {
		setSrc(src)
		copyTexture(dstOrigin = dstOrigin, dstTR = dstTR)
	}

	fun copyOntoDst(src: Texture3D, layer: Int, dstOrigin: Vec2i = Vec2i(0), dstTR: Vec2i = dstOrigin + Vec2i(src.size)) {
		setSrc(src, layer)
		copyTexture(dstOrigin = dstOrigin, dstTR = dstTR)
	}

	abstract fun execute()
}