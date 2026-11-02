package com.pineypiney.game_engine.rendering.cameras

import com.pineypiney.game_engine.util.extension_functions.coerceIn
import glm_.detail.GLM_DEPTH_CLIP_SPACE
import glm_.detail.GlmDepthClipSpace
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class Camera(
	override var aspectRatio: Float,
	pos: Vec3 = Vec3(0, 0, 5),
	up: Vec3 = Vec3(0, 1, 0),
) : CameraI {

	override var cameraPos = Vec3(); protected set
	override val cameraUp = up

	override val cameraFront = Vec3(0, 0, -1)
	override var cameraRight = Vec3()

	override var cameraMinPos = Vec3(-Float.MAX_VALUE)
	override var cameraMaxPos = Vec3(Float.MAX_VALUE)

	override var range = Vec2(1000f, 0.1f)

	init {
		this.setPos(pos)
	}

	override fun init() {
		updateCameraRight()
	}

	override fun setPos(pos: Vec3) {
		cameraPos = pos.coerceIn(cameraMinPos, cameraMaxPos)
	}

	override fun updateAspectRatio(aspectRatio: Float) {
		this.aspectRatio = aspectRatio
	}

	override fun getView(mat: Mat4): Mat4 {
		return glm.lookAt(cameraPos, cameraPos.plus(cameraFront), cameraUp, mat)
	}

	override fun getMaxDepth(): Float {
		return if(range.y > range.x) 1f
		else when(GLM_DEPTH_CLIP_SPACE){
			GlmDepthClipSpace.ZERO_TO_ONE -> 0f
			GlmDepthClipSpace.NEGATIVE_ONE_TO_ONE -> -1f
		}
	}

	override fun delete() {

	}
}