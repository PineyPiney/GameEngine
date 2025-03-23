package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.window.Viewport
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

open class PixelTransformComponent(parent: GameObject, pixelPos: Vec2i, pixelScale: Vec2i, origin: Vec2 = Vec2(-1f), var screenRelative: Boolean = false) :
	TransformComponent(parent), PreRenderComponent, UpdatingAspectRatioComponent
{

	override val whenVisible: Boolean = false
	var dirtyPixelValues = true

	var pixelPos: Vec2i = pixelPos
		set(value) {
			field = value
			dirtyPixelValues = true
		}

	var pixelScale: Vec2i = pixelScale
		set(value) {
			field = value
			dirtyPixelValues = true
		}

	var origin: Vec2 = origin
		set(value) {
			field = value
			dirtyPixelValues = true
		}

	var parentScale = Vec2(-1f)

	fun recalculateScale(frameSize: Vec2i){
		if(screenRelative) worldScale = Vec3(pixelToRel(pixelScale, frameSize), worldScale.z)
		else scale = Vec3(pixelToRel(pixelScale, frameSize), scale.z)
	}
	fun recalculatePosition(frameSize: Vec2i) {
		if(screenRelative || parent.parent == null) worldPosition = Vec3(pixelToRel(pixelPos, frameSize) + Vec2((origin.x * frameSize.x) / frameSize.y, origin.y), worldPosition.z)
		else position = Vec3(pixelToRel(pixelPos, frameSize) + origin, position.z)
	}

	fun pixelToRel(pix: Vec2i, frameSize: Vec2i): Vec2{
		val p = parent.parent
		return if(screenRelative || p == null) Vec2(pix) * (2f / frameSize.y)
		else {
			val x = if(parentScale.x == 0f) 0f else 2f * pix.x / (frameSize.y * parentScale.x)
			val y = if(parentScale.y == 0f) 0f else 2f * pix.y / (frameSize.y * parentScale.y)
			return Vec2(x, y)
		}
	}

	fun updateSize(frameSize: Vec2i){
		dirtyPixelValues = false
		recalculateScale(frameSize)
		recalculatePosition(frameSize)
	}

	fun updateParentSize(){
		val gp = parent.parent
		val gps = if(gp == null) Vec2(1f)
		else Vec2(gp.transformComponent.worldScale)

		if(gps != parentScale){
			parentScale = gps
			dirtyPixelValues = true
		}
	}

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		updateParentSize()
		if(dirtyPixelValues) updateSize(renderer.viewportSize)
	}

	override fun updateAspectRatio(view: Viewport) {
		updateParentSize()
		updateSize(view.size)
	}
}