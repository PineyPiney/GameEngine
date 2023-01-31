package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.util.extension_functions.coerceIn
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class Camera(override val window: WindowI, pos: Vec3 = Vec3(0, 0, 5), up: Vec3 = Vec3(0, 1, 0)): ICamera {

    override var cameraPos = Vec3(); protected set
    override var cameraUp = up

    override val cameraFront = Vec3(0, 0, -1)
    override var cameraRight = Vec3()

    override var cameraMinPos = Vec3(-Float.MAX_VALUE)
    override var cameraMaxPos = Vec3(Float.MAX_VALUE)

    override var range = Vec2(0.1, 1000)

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

    open fun updateCameraVectors() {
        cameraRight = glm.cross(cameraFront, cameraUp).normalize()
    }

    override fun updateAspectRatio() {}

    override fun getView(): Mat4 {
        return glm.lookAt(cameraPos, cameraPos.plus(cameraFront), cameraUp)
    }

    override fun delete() {

    }
}