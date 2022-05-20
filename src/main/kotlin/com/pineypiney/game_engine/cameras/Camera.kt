package com.pineypiney.game_engine.cameras

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.util.extension_functions.coerceIn
import com.pineypiney.game_engine.util.extension_functions.round
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

open class Camera(pos: Vec3 = Vec3(0, 0, 5), up: Vec3 = Vec3(0, 1, 0), yaw: Double = -90.0, pitch: Double = 0.0, fov: Float = 90f) {

    var cameraPos = Vec3()
    var cameraUp = up

    var cameraFront = Vec3()
    var cameraRight = Vec3()

    var cameraMinPos = Vec3(-Float.MAX_VALUE)
    var cameraMaxPos = Vec3(Float.MAX_VALUE)

    var cameraYaw = yaw; var cameraPitch = pitch
    var lastX = 0.0; var lastY = 0.0
    var movementSpeed = 0.0; var mouseSensitivity = 0.0
    var FOV = fov

    var range = Vec2(0.1, 1000)

    init {
        this.setPos(pos)
        cameraFront = Vec3(0.0f, 0.0f, -1.0f)
        movementSpeed = 10.0
        mouseSensitivity = 1.0
        lastX = 250.0
        lastY = 250.0

    }

    fun init() {
        updateCameraVectors()
    }

    open fun setPos(pos: Vec3){
        this.cameraPos = pos.coerceIn(cameraMinPos, cameraMaxPos)
    }

    open fun setCameraFOV(FOV: Float){
        this.FOV = glm.clamp(FOV, 0.1f, 180f)
    }

    open fun getViewMatrix(): Mat4 {
        return glm.lookAt(cameraPos, cameraPos.plus(cameraFront), cameraUp)
    }

    fun getSpanAtDistance(distance: Float = cameraPos.z): Vec2{
        val backgroundVerticalSpan = 2 * distance * tan(FOV.rad * 0.5)
        val backgroundHorizontalSpan = backgroundVerticalSpan * Window.INSTANCE.aspectRatio
        return Vec2(backgroundHorizontalSpan, backgroundVerticalSpan)
    }

    fun screenToWorld(pos: Vec2, distance: Float = cameraPos.z): Vec2{
        return pos * getSpanAtDistance(distance) * 0.5 + Vec2(cameraPos)
    }

    fun worldToScreen(pos: Vec2, distance: Float = cameraPos.z): Vec2{
        return (pos - Vec2(cameraPos)) / (getSpanAtDistance(distance) * 0.5)
    }

    open fun updateCameraVectors() {
        cameraFront.x = cos(glm.radians(cameraYaw)).toFloat() * cos(glm.radians(cameraPitch)).toFloat()
        cameraFront.y = sin(glm.radians(cameraPitch)).toFloat()
        cameraFront.z = sin(glm.radians(cameraYaw)).toFloat() * cos(glm.radians(cameraPitch)).toFloat()
        cameraFront.normalize()
        cameraRight = glm.cross(cameraFront, cameraUp).normalize()
    }

    fun posToText() : String{
        return "${cameraPos.x.round(2)}, ${cameraPos.y.round(2)}, ${cameraPos.z.round(2)}"
    }

    fun angleToText() : String{
        return "Yaw: ${cameraYaw.round(2)}, Pitch: ${cameraPitch.round(2)},"
    }
}