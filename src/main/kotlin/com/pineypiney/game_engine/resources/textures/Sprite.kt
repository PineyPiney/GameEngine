package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import glm_.vec2.Vec2
import kotlin.math.max

class Sprite(val texture: Texture, ppu: Float, val spriteCenter: Vec2 = Vec2(.5f), val origin: Vec2 = Vec2(), val size: Vec2 = Vec2(1f), var flipX: Boolean = false, var flipY: Boolean = false) {

	var pixelsPerUnit: Float = ppu
		set(value) {
			field = max(value, 0.000001f)
			pixelSize = 1f / field
			mesh = calculateMesh()
		}
	var pixelSize: Float = 1f / pixelsPerUnit; private set


	val pixelWidth get() = texture.width * size.x
	val pixelHeight get() = texture.height * size.y

	val renderWidth get() = texture.width * size.x * pixelSize
	val renderHeight get() = texture.height * size.y * pixelSize

	var mesh: Mesh = calculateMesh()

	fun positionMesh(spriteCenter: Vec2, origin: Vec2, size: Vec2, flipX: Boolean = this.flipX, flipY: Boolean = this.flipY){
		this.spriteCenter.put(spriteCenter)
		this.origin.put(origin)
		this.size.put(size)

		this.flipX = flipX
		this.flipY = flipY

		mesh = calculateMesh()
	}

	fun calculateMesh(): Mesh {
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
		return SquareShape(bl, tr, o, o + s)
	}
}