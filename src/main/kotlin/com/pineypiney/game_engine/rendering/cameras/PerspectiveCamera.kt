package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.cos
import kotlin.math.sin
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

    fun screenToWorld(pos: Vec2, distance: Float): Vec2 {
        return pos * getSpan() * distance * 0.5 + Vec2(cameraPos)
    }

    fun worldToScreen(pos: Vec2, distance: Float): Vec2 {
        return (pos - Vec2(cameraPos)) / (getSpan() * distance * 0.5)
    }

    override fun getProjection(): Mat4 = glm.perspective(FOV.rad, window.aspectRatio, range.x, range.y)

    override fun getSpan(): Vec2 {
        val backgroundVerticalSpan = 2 * tan(FOV.rad * 0.5)
        val backgroundHorizontalSpan = backgroundVerticalSpan * window.aspectRatio
        return Vec2(backgroundHorizontalSpan, backgroundVerticalSpan)
    }

    override fun getRay(point: Vec2): Ray {
        val yaw = Math.toRadians(point.x * FOV / 2.0 * window.aspectRatio)
        val pitch = Math.toRadians(point.y * FOV / 2.0)
        val direction = eulerToVector(cameraYaw + yaw, cameraPitch + pitch)
        return Ray(cameraPos, direction)
    }

    override fun updateCameraVectors() {
        eulerToVector(cameraYaw, cameraPitch, cameraFront)
        super.updateCameraVectors()
    }

    companion object{
        fun eulerToVector(yaw: Double, pitch: Double, res: Vec3 = Vec3()): Vec3{
            res.x = (cos(Math.toRadians(yaw)) * cos(Math.toRadians(pitch))).toFloat()
            res.y = sin(Math.toRadians(pitch)).toFloat()
            res.z = (sin(Math.toRadians(yaw)) * cos(Math.toRadians(pitch))).toFloat()
            res.normalize()
            return res
        }
    }
}