package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class PerspectiveCamera(
	aspectRatio: Float,
	pos: Vec3 = Vec3(0, 0, 5),
	up: Vec3 = Vec3(0, 1, 0),
	yaw: Double = -90.0,
	pitch: Double = 0.0,
	fov: Float = 60f
) : Camera(aspectRatio, pos, up, yaw, pitch) {

	constructor(
		window: WindowI,
		pos: Vec3 = Vec3(0, 0, 5),
		up: Vec3 = Vec3(0, 1, 0),
		yaw: Double = -90.0,
		pitch: Double = 0.0,
		fov: Float = 60f
	) : this(window.aspectRatio, pos, up, yaw, pitch, fov)

	var FOV = fov
		private set(value) {
			field = glm.clamp(value, 0.1f, 180f)
		}

	override fun getProjection(mat: Mat4): Mat4 = glm.perspective(FOV.rad, aspectRatio, range.x, range.y, mat)

	override fun getRay(point: Vec2): Ray {
		val worldPos = screenToWorld(point)
		val dir = (worldPos - cameraPos).normalize()
		if (cameraPos dot cameraFront > 0f) dir *= -1f
		return Ray(cameraPos, dir)
	}
}