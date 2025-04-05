package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.objects.util.meshes.SquareMesh
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import kotlin.math.max

class Sprite(texture: Texture, ppu: Float, spriteCenter: Vec2 = Vec2(.5f), origin: Vec2 = Vec2(), size: Vec2 = Vec2(1f), flipX: Boolean = false, flipY: Boolean = false) {

	constructor(texture: Texture, ppu: Float, spriteCenter: Vec2i, origin: Vec2i = Vec2i(), size: Vec2i = texture.size, flipX: Boolean = false, flipY: Boolean = false) : this(texture, ppu, Vec2(spriteCenter) / texture.size, Vec2(origin) / texture.size, Vec2(size) / texture.size, flipX, flipY)

	var texture: Texture = texture
		set(value) {
			field = value
			recalculateMesh()
		}

	var pixelsPerUnit: Float = ppu
		set(value) {
			field = max(value, 0.000001f)
			pixelSize = 1f / field
			recalculateMesh()
		}
	var pixelSize: Float = 1f / pixelsPerUnit; private set

	var spriteCenter: Vec2 = spriteCenter
		set(value) {
			field = value
			recalculateMesh()
		}

	var origin: Vec2 = origin
		set(value) {
			field = value
			recalculateMesh()
		}

	var size: Vec2 = size
		set(value) {
			field = value
			recalculateMesh()
		}

	var flipX: Boolean = flipX
		set(value) {
			field = value
			recalculateMesh()
		}

	var flipY: Boolean = flipY
		set(value) {
			field = value
			recalculateMesh()
		}

	val pixelWidth get() = texture.width * size.x
	val pixelHeight get() = texture.height * size.y

	val renderWidth get() = texture.width * size.x * pixelSize
	val renderHeight get() = texture.height * size.y * pixelSize

	var mesh: Mesh = calculateMesh(); private set

	private fun calculateMesh(): Mesh {
		val renderSize = Vec2(renderWidth, renderHeight)

		val bl = -spriteCenter * renderSize
		val o = Vec2(origin)
		val s = Vec2(size)
		if(flipX) {
			o.x += s.x
			s.x = -s.x
		}
		if(flipY) {
			o.y += s.y
			s.y = -s.y
		}
		val tr = bl + renderSize
		return SquareMesh(bl, tr, o, o + s)
	}
	
	fun recalculateMesh(){
		mesh.delete()
		mesh = calculateMesh()
	}

	override fun toString(): String {
		return "Sprite(${texture.fileName})"
	}
}