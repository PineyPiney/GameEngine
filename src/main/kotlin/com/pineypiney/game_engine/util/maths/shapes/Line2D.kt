package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.rotationComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class Line2D(val start: Vec2, val end: Vec2): Shape() {
    val grad = (end - start).normalize()

    override fun intersectedBy(ray: Ray): Array<Vec3> {
        return arrayOf()
    }

    override fun containsPoint(point: Vec3): Boolean {
        return false
    }

    override fun transformedBy(model: Mat4): Shape {
        val s = end - start
        val m = model.rotationComponent().scale(model.getScale())
        val newStart = start + Vec2(model.getTranslation())
        val newS = Vec2(m * Vec4(s, 0f, 1f))
        return Line2D(newStart, newStart + newS)
    }

    infix fun vectorTo(point: Vec2): Vec2 {
        val vec = end - start
        val p = point - start
        val proj = vec dot p
        val delta = proj / vec.length2()

        return if(delta > 1f) point - end
        else if(delta < 0f) p
        else point - (start + (vec * delta))
    }
}