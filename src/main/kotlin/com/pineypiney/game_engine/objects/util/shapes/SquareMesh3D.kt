package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.util.maths.shapes.Rect3D
import com.pineypiney.game_engine.util.maths.shapes.Shape3D
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class SquareMesh3D(origin: Vec3, side1: Vec3, side2: Vec3, tbl: Vec2 = Vec2(), ttr: Vec2 = Vec2(1)) :
	IndicesMesh(createVertices(origin, side1, side2, tbl, ttr), intArrayOf(3, 2), intArrayOf(0, 1, 2, 0, 2, 3)) {

	override val shape: Shape3D = Rect3D(bl, Vec3(15, getVertices()) - bl, Vec3(5, getVertices()) - bl)

	val bl get() = Vec3(0, getVertices())

	init {
		vertexSize = 5
		positionSize = 3
		textureSize = 2
		textureOffset = 3
	}

	companion object {
		fun createVertices(origin: Vec3, side1: Vec3, side2: Vec3, to: Vec2, tf: Vec2): FloatArray {
			return floatArrayOf(
				origin.x, origin.y, origin.z, to.x, to.y,
				origin.x + side1.x, origin.y + side1.y, origin.z + side1.z, to.x, tf.y,
				origin.x + side1.x + side2.x, origin.y + side1.y + side2.y, origin.z + side1.z + side2.z, tf.x, tf.y,
				origin.x + side2.x, origin.y + side2.y, origin.z + side2.z, tf.x, to.y
			)
		}
	}
}