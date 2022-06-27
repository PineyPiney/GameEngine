package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.Window
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class OrthographicCamera(window: Window, pos: Vec3 = Vec3(0, 0, 5), up: Vec3 = Vec3(0, 1, 0), val height: Float = 10f): Camera(window, pos, up) {

    override var range: Vec2 = Vec2(-0.1, -1000)

    override fun getSpanAtDistance(distance: Float): Vec2 {
        return Vec2(window.aspectRatio, 1) * height
    }

    override fun screenToWorld(pos: Vec2, distance: Float): Vec2 {
        return pos * getSpanAtDistance(distance) * 0.5 + Vec2(cameraPos)
    }

    override fun worldToScreen(pos: Vec2, distance: Float): Vec2 {
        return (pos - Vec2(cameraPos)) / (getSpanAtDistance(distance) * 0.5)
    }

    override fun getPerspective(): Mat4{
        val extents = getSpanAtDistance(0f) / 2
        val (l, b) = Vec2(cameraPos) - extents
        val (r, t) = Vec2(cameraPos) + extents
        val (n, f) = range + Vec2(cameraPos.z)
        return glm.ortho(l, r, b, t, n, f)

    }

}