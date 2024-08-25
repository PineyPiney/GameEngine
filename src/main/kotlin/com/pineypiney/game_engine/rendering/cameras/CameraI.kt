package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

interface CameraI : Initialisable {

	val aspectRatio: Float

	val cameraPos: Vec3
	val cameraUp: Vec3

	val cameraFront: Vec3
	val cameraRight: Vec3

	val cameraMinPos: Vec3
	val cameraMaxPos: Vec3

	val range: Vec2

	fun updateAspectRatio(aspectRatio: Float)
	fun getView(mat: Mat4 = Mat4()): Mat4
	fun getProjection(mat: Mat4 = Mat4()): Mat4
	fun getRay(point: Vec2): Ray
}