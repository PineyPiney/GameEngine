package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.util.maths.shapes.AxisAlignedCuboid
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class CubeShape(blf: Vec3, trb: Vec3, tbl: Vec2 = Vec2(), ttr: Vec2 = Vec2(1)): ArrayShape(createVertices(blf, trb, tbl, ttr), intArrayOf(3, 3, 2)) {

    override val shape: AxisAlignedCuboid = AxisAlignedCuboid((blf + trb) * 0.5f, trb - blf)

    companion object{
        fun createVertices(blf: Vec3, trb: Vec3, to: Vec2, tf: Vec2): FloatArray{
            return floatArrayOf(
                // positions            // normals          // texture co-ords
                // Back
                trb.x,  blf.y,  blf.z,  0.0,  0.0, -1.0,    tf.x, to.y,
                trb.x,  trb.y,  blf.z,  0.0,  0.0, -1.0,    tf.x, tf.y,
                blf.x,  trb.y,  blf.z,  0.0,  0.0, -1.0,    to.x, tf.y,
                blf.x,  trb.y,  blf.z,  0.0,  0.0, -1.0,    to.x, tf.y,
                blf.x,  blf.y,  blf.z,  0.0,  0.0, -1.0,    to.x, to.y,
                trb.x,  blf.y,  blf.z,  0.0,  0.0, -1.0,    tf.x, to.y,

                // Front
                blf.x,  blf.y,  trb.z,  0.0,  0.0,  1.0,    to.x, to.y,
                blf.x,  trb.y,  trb.z,  0.0,  0.0,  1.0,    to.x, tf.y,
                trb.x,  trb.y,  trb.z,  0.0,  0.0,  1.0,    tf.x, tf.y,
                trb.x,  trb.y,  trb.z,  0.0,  0.0,  1.0,    tf.x, tf.y,
                trb.x,  blf.y,  trb.z,  0.0,  0.0,  1.0,    tf.x, to.y,
                blf.x,  blf.y,  trb.z,  0.0,  0.0,  1.0,    to.x, to.y,

                // Left
                blf.x,  blf.y,  blf.z,  1.0,  0.0,  0.0,    to.x, to.y,
                blf.x,  trb.y,  blf.z,  1.0,  0.0,  0.0,    to.x, tf.y,
                blf.x,  trb.y,  trb.z,  1.0,  0.0,  0.0,    tf.x, tf.y,
                blf.x,  trb.y,  trb.z,  1.0,  0.0,  0.0,    tf.x, tf.y,
                blf.x,  blf.y,  trb.z,  1.0,  0.0,  0.0,    tf.x, to.y,
                blf.x,  blf.y,  blf.z,  1.0,  0.0,  0.0,    to.x, to.y,

                // Right
                trb.x,  blf.y,  trb.z,  1.0,  0.0,  1.0,    to.x, to.y,
                trb.x,  trb.y,  trb.z,  1.0,  0.0,  1.0,    to.x, tf.y,
                trb.x,  trb.y,  blf.z,  1.0,  0.0,  1.0,    tf.x, tf.y,
                trb.x,  trb.y,  blf.z,  1.0,  0.0,  1.0,    tf.x, tf.y,
                trb.x,  blf.y,  blf.z,  1.0,  0.0,  1.0,    tf.x, to.y,
                trb.x,  blf.y,  trb.z,  1.0,  0.0,  1.0,    to.x, to.y,

                // Bottom
                blf.x,  blf.y,  blf.z,  0.0, -1.0,  0.0,    to.x, to.y,
                blf.x,  blf.y,  trb.z,  0.0, -1.0,  0.0,    to.x, tf.y,
                trb.x,  blf.y,  trb.z,  0.0, -1.0,  0.0,    tf.x, tf.y,
                trb.x,  blf.y,  trb.z,  0.0, -1.0,  0.0,    tf.x, tf.y,
                trb.x,  blf.y,  blf.z,  0.0, -1.0,  0.0,    tf.x, to.y,
                blf.x,  blf.y,  blf.z,  0.0, -1.0,  0.0,    to.x, to.y,

                // Top
                blf.x,  trb.y,  trb.z,  0.0,  1.0,  0.0,    to.x, to.y,
                blf.x,  trb.y,  blf.z,  0.0,  1.0,  0.0,    to.x, tf.y,
                trb.x,  trb.y,  blf.z,  0.0,  1.0,  0.0,    tf.x, tf.y,
                trb.x,  trb.y,  blf.z,  0.0,  1.0,  0.0,    tf.x, tf.y,
                trb.x,  trb.y,  trb.z,  0.0,  1.0,  0.0,    tf.x, to.y,
                blf.x,  trb.y,  trb.z,  0.0,  1.0,  0.0,    to.x, to.y,
            )
        }
    }
}