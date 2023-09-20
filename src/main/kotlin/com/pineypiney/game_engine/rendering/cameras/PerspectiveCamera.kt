package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.util.maths.eulerToVector
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.tan

open class PerspectiveCamera(window: WindowI, pos: Vec3 = Vec3(0, 0, 5), up: Vec3 = Vec3(0, 1, 0), yaw: Double = -90.0, pitch: Double = 0.0, fov: Float = 90f): Camera(window, pos, up) {

    // CameraFront is now a var
    override var cameraFront: Vec3 = super.cameraFront

    var cameraYaw = yaw
    var cameraPitch = pitch
    var FOV = fov
        private set(value){
            field = glm.clamp(value, 0.1f, 180f)
        }

    fun screenToWorld(screenPos: Vec2): Vec3 {
        val pv = getProjection() * getView()
        val invPV = pv.inverse()
        val pos = Vec4(screenPos * pv[3, 3], pv[3, 2], pv[3, 3])
        val worldPos = invPV * pos
        return Vec3(worldPos)
    }

    fun worldToScreen(worldPos: Vec3): Vec2 {
        val pv = getProjection() * getView()
        val pos = pv * Vec4(worldPos, 1)
        return Vec2(pos / pos.w)
    }

    override fun getProjection(): Mat4 = glm.perspective(FOV.rad, window.aspectRatio, range.x, range.y)

    override fun getSpan(): Vec2 {
        val backgroundVerticalSpan = 2 * tan(FOV.rad * 0.5)
        val backgroundHorizontalSpan = backgroundVerticalSpan * window.aspectRatio
        return Vec2(backgroundHorizontalSpan, backgroundVerticalSpan)
    }

    override fun getRay(point: Vec2): Ray {
        val worldPos = screenToWorld(point)
        val dir = (worldPos - cameraPos).normalize()
        return Ray(cameraPos, dir)
    }

    override fun updateCameraVectors() {
        eulerToVector(Math.toRadians(cameraYaw), Math.toRadians(cameraPitch), cameraFront)
        super.updateCameraVectors()
    }
}