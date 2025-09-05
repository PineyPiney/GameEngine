package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.util.extension_functions.projectOn
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.max

open class OrthographicCamera(
	aspectRatio: Float,
	pos: Vec3 = Vec3(0, 0, 5),
	up: Vec3 = Vec3(0, 1, 0),
	height: Float = 10f
) : Camera(aspectRatio, pos, up) {

	constructor(
		window: WindowI,
		pos: Vec3 = Vec3(0, 0, 5),
		up: Vec3 = Vec3(0, 1, 0),
		height: Float = 10f
	) : this(window.aspectRatio, pos, up, height)

	var height: Float = height
		set(value) {
			field = max(value, 0.001f)
		}

	override fun getProjection(mat: Mat4): Mat4 {
		val extents = Vec2(height * .5f * aspectRatio, height * .5f)
		return glm.ortho(-extents.x, extents.x, -extents.y, extents.y, range.x, range.y, mat)
	}

	override fun getRay(point: Vec2): Ray {
		val worldPos = screenToWorld(point)
		val origin = worldPos + ((cameraPos - worldPos) projectOn cameraFront)
		return Ray(origin, cameraFront)
	}
}