package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

interface CameraI : Initialisable {

	val aspectRatio: Float

	val cameraPos: Vec3
	val cameraMinPos: Vec3
	val cameraMaxPos: Vec3

	val cameraUp: Vec3
	val cameraFront: Vec3
	val cameraRight: Vec3

	val range: Vec2

	fun getView(mat: Mat4 = Mat4()): Mat4
	fun getProjection(mat: Mat4 = Mat4()): Mat4
	fun getRay(point: Vec2): Ray

	fun updateAspectRatio(aspectRatio: Float)

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
		return Vec2(pos) / pos.w
	}

	fun updateCameraRight() {
		cameraFront.cross(cameraUp).normalize(cameraRight)
	}
}