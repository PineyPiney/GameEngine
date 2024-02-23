package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2

open class SquareShape(bl: Vec2, tr: Vec2, tbl: Vec2 = Vec2(), ttr: Vec2 = Vec2(1)): IndicesShape(createVertices(bl, tr, tbl, ttr), intArrayOf(2, 2), intArrayOf(0, 1, 2, 0, 2, 3)) {

    override val shape: Shape = Rect2D(bl, tr - bl)

    companion object{
        fun createVertices(bl: Vec2, tr: Vec2, to: Vec2, tf: Vec2): FloatArray{
            return floatArrayOf(
                bl.x, bl.y, to.x, to.y,
                bl.x, tr.y, to.x, tf.y,
                tr.x, tr.y, tf.x, tf.y,
                tr.x, bl.y, tf.x, to.y
            )
        }
    }
}