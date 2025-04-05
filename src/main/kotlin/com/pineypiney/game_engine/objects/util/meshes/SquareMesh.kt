package com.pineypiney.game_engine.objects.util.meshes

import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.vec2.Vec2

open class SquareMesh(bl: Vec2, tr: Vec2, tbl: Vec2 = Vec2(), ttr: Vec2 = Vec2(1)) :
	IndicesMesh(createVertices(bl, tr, tbl, ttr), arrayOf(VertexAttribute.POSITION2D, VertexAttribute.TEX_COORD), intArrayOf(0, 1, 2, 0, 2, 3)) {

	override val shape: Shape2D = Rect2D(bl, tr - bl)

	val bl get() = getAttribute(VertexAttribute.POSITION2D).first()
	val tr get() = getAttribute(VertexAttribute.POSITION2D)[2]

	companion object {
		fun createVertices(bl: Vec2, tr: Vec2, to: Vec2, tf: Vec2): FloatArray {
			return floatArrayOf(
				bl.x, bl.y, to.x, to.y,
				bl.x, tr.y, to.x, tf.y,
				tr.x, tr.y, tf.x, tf.y,
				tr.x, bl.y, tf.x, to.y
			)
		}
	}
}