package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.rendering.RendererI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

open class PixelTransformComponent(parent: GameObject, pixelPos: Vec2i, pixelScale: Vec2i, origin: Vec2 = Vec2(-1f), var screenRelative: Boolean = false) :
	TransformComponent(parent), PreRenderComponent, UpdatingAspectRatioComponent
{

	var frameSize: Vec2i = Vec2i(-1)
	override val whenVisible: Boolean = false

	var pixelPos: Vec2i = pixelPos
		set(value) {
			field = value
			recalculatePosition()
		}

	var origin: Vec2 = origin
		set(value) {
			field = value
			recalculatePosition()
		}

	var pixelScale: Vec2i = pixelScale
		set(value) {
			field = value
			recalculateScale()
		}

	val aspectRatio get() = frameSize.x.toFloat() / frameSize.y

	fun recalculateScale(){
		if(screenRelative || parent.parent == null) worldScale = Vec3(pixelToRel(pixelScale), worldScale.z)
		else scale = Vec3(pixelToRel(pixelScale), scale.z)
	}
	fun recalculatePosition() {
		if(screenRelative || parent.parent == null) worldPosition = Vec3(pixelToRel(pixelPos) + Vec2(origin.x * aspectRatio, origin.y), worldPosition.z)
		else position = Vec3(pixelToRel(pixelPos) + origin, position.z)
	}

	fun pixelToRel(pix: Vec2i): Vec2{
		val p = parent.parent
		return if(screenRelative || p == null) Vec2(2f * pix.x * aspectRatio / frameSize.x, 2f * pix.y / frameSize.y)
		else Vec2(2f * pix.x / (frameSize.x * p.transformComponent.worldScale.x), 2f * pix.y / (frameSize.y * p.transformComponent.worldScale.y))
	}

	fun updateSize(size: Vec2i){
		frameSize = size
		recalculateScale()
		recalculatePosition()
	}

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		if(frameSize.x < 0) updateSize(renderer.viewportSize)
	}

	override fun updateAspectRatio(renderer: RendererI) {
		updateSize(renderer.viewportSize)
	}
}