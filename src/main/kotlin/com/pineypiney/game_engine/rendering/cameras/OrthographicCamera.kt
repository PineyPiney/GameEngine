package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.max

open class OrthographicCamera(window: WindowI, pos: Vec3 = Vec3(0, 0, 5), up: Vec3 = Vec3(0, 1, 0), height: Float = 10f): Camera(window, pos, up) {

    protected var height: Float = height
        set(value) { field = max(value, 0.001f) }

    fun screenToWorld(pos: Vec2): Vec2 {
        return pos * height * 0.5 + Vec2(cameraPos)
    }

    fun worldToScreen(pos: Vec2): Vec2 {
        return (pos - Vec2(cameraPos)) / (getSpan() * 0.5)
    }

    override fun getProjection(mat: Mat4): Mat4{
        val extents = getSpan() / 2
        return glm.ortho(-extents.x, extents.x, -extents.y, extents.y, range.x, range.y, mat)

    }

    override fun getSpan(): Vec2 {
        return Vec2(window.aspectRatio * height, height)
    }

    override fun getRay(point: Vec2): Ray {
        return Ray(Vec3(screenToWorld(point), cameraPos.z), cameraFront)
    }
}