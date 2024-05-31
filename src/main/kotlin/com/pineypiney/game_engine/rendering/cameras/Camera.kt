package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.util.extension_functions.coerceIn
import com.pineypiney.game_engine.util.maths.eulerToVector
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

abstract class Camera(override val window: WindowI, pos: Vec3 = Vec3(0, 0, 5), up: Vec3 = Vec3(0, 1, 0), yaw: Double = -90.0, pitch: Double = 0.0): CameraI {

    override var cameraPos = Vec3(); protected set
    override val cameraUp = up

    override val cameraFront = Vec3(0, 0, -1)
    override var cameraRight = Vec3()

    override var cameraMinPos = Vec3(-Float.MAX_VALUE)
    override var cameraMaxPos = Vec3(Float.MAX_VALUE)

    override var range = Vec2(0.1, 1000)

    var cameraYaw = yaw
    var cameraPitch = pitch

    init {
        this.setPos(pos)
    }

    override fun init() {
        updateCameraVectors()
    }

    open fun setPos(pos: Vec3){
        cameraPos = pos.coerceIn(cameraMinPos, cameraMaxPos)
    }

    open fun translate(vec: Vec3){
        cameraPos = (cameraPos + vec).coerceIn(cameraMinPos, cameraMaxPos)
    }

    open fun translate(vec: Vec2) = translate(Vec3(vec))

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

    open fun updateCameraVectors() {
        updateCameraFront()
        updateCameraRight()
    }

    open fun updateCameraFront(){
        eulerToVector(Math.toRadians(cameraYaw), Math.toRadians(cameraPitch), cameraFront)
    }

    open fun updateCameraRight(){
        cameraRight = glm.cross(cameraFront, cameraUp).normalize()
    }

    override fun updateAspectRatio() {}

    override fun getView(mat: Mat4): Mat4 {
        return glm.lookAt(cameraPos, cameraPos.plus(cameraFront), cameraUp, mat)
    }

    override fun delete() {

    }
}