package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.extension_functions.rotationComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.abs

class Line(val start: Vec3, val end: Vec3): Shape() {
    val grad = (end - start).normalize()

    // https://stackoverflow.com/a/8862483
    infix fun intersects(plane: Plane): Vec3?{

        val dot = plane.normal dot grad

        // If the direction is perpendicular to the normal then they do not cross over
        if(abs(dot) < 1e-6) return null

        // a is the distance along the ray to the intersection with the plane of the rectangle
        val a = ((plane.point - start) dot plane.normal) / dot

        // return the point on the plane
        return start + grad * a
    }

    override fun intersectedBy(ray: Ray): Array<Vec3> {
        return arrayOf()
    }

    override fun containsPoint(point: Vec3): Boolean {
        return false
    }

    override fun vectorTo(point: Vec3): Vec3 {
        val op = point - start
        val side = (end - start)

        val a = op dot (end - start)
        val x: Vec3 = if(a < 0) Vec3(0f)
        else if(a > side.length()) side
        else op projectOn side

        return start + x
    }

    override fun transformedBy(model: Mat4): Shape {
        val s = end - start
        val m = model.rotationComponent().scale(model.getScale())
        val newStart = start + model.getTranslation()
        val newS = Vec3(m * Vec4(s, 1f))
        return Line(newStart, newStart + newS)
    }
}