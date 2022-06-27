package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.Window
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

open class PerspectiveCamera(window: Window, pos: Vec3 = Vec3(0, 0, 5), up: Vec3 = Vec3(0, 1, 0),  yaw: Double = -90.0, pitch: Double = 0.0, fov: Float = 90f): Camera(window, pos, up) {

    // CameraFront is now a var
    override var cameraFront: Vec3 = super.cameraFront

    var cameraYaw = yaw
    var cameraPitch = pitch
    var FOV = fov; private set

    open fun setCameraFOV(FOV: Float){
        this.FOV = glm.clamp(FOV, 0.1f, 180f)
    }

    override fun getSpanAtDistance(distance: Float): Vec2 {
        val backgroundVerticalSpan = 2 * distance * tan(FOV.rad * 0.5)
        val backgroundHorizontalSpan = backgroundVerticalSpan * window.aspectRatio
        return Vec2(backgroundHorizontalSpan, backgroundVerticalSpan)
    }

    override fun screenToWorld(pos: Vec2, distance: Float): Vec2 {
        return pos * getSpanAtDistance(distance) * 0.5 + Vec2(cameraPos)
    }

    override fun worldToScreen(pos: Vec2, distance: Float): Vec2 {
        return (pos - Vec2(cameraPos)) / (getSpanAtDistance(distance) * 0.5)
    }

    open fun getViewMatrix(): Mat4 {
        return glm.lookAt(cameraPos, cameraPos.plus(cameraFront), cameraUp)
    }

    override fun getPerspective(): Mat4 = glm.perspective(FOV.rad, window.aspectRatio, range.x, range.y)

    override fun updateCameraVectors() {
        cameraFront.x = cos(glm.radians(cameraYaw)).toFloat() * cos(glm.radians(cameraPitch)).toFloat()
        cameraFront.y = sin(glm.radians(cameraPitch)).toFloat()
        cameraFront.z = sin(glm.radians(cameraYaw)).toFloat() * cos(glm.radians(cameraPitch)).toFloat()
        cameraFront.normalize()
        super.updateCameraVectors()
    }
}