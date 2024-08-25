package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class RelativeTransformComponent(
	parent: GameObject, relPos: Vec2, relScale: Vec2, origin: Vec2 = Vec2(), var aspectRatio: Float = 1f
) : TransformComponent(parent), UpdatingAspectRatioComponent {

	var relPos: Vec2 = relPos
		set(value) {
			field = value
			recalculatePosition()
		}
	var origin: Vec2 = origin
		set(value) {
			field = value
			recalculatePosition()
		}

	var relScale: Vec2 = relScale
		set(value) {
			field = value
			recalculateScale()
			recalculatePosition()
		}

	fun recalculateScale(){
		scale = Vec3(relScale.x * aspectRatio, relScale.y, transform.scale.z)
	}
	fun recalculatePosition() {
		position = Vec3(relPos.x * aspectRatio, relPos.y, transform.position.z) - (Vec3(origin, 0) * transform.scale)
	}

	override fun updateAspectRatio(renderer: RendererI) {
		aspectRatio = renderer.aspectRatio
		recalculateScale()
		recalculatePosition()
	}
}