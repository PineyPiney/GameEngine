package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

interface ICamera: Initialisable {

    val window: Window

    val cameraPos: Vec3
    val cameraUp: Vec3

    val cameraFront: Vec3
    val cameraRight: Vec3

    val cameraMinPos: Vec3
    val cameraMaxPos: Vec3

    val range: Vec2

    fun updateAspectRatio()
    fun getView(): Mat4
    fun getProjection(): Mat4
    fun getSpan(): Vec2
    fun getRay(point: Vec2 = window.input.mouse.lastPos): Ray
}