package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.objects.components.RelativeTransformComponent
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

open class MenuItem(name: String = "MenuItem") : GameObject(name) {

	override fun init() {
		super.init()
		layer = 1
	}

	fun os(origin: Vec2, size: Vec2) {
		position = Vec3(origin, 0f)
		scale = Vec3(size, 1f)
	}

	fun os(origin: Vec3, size: Vec2) {
		position = origin
		scale = Vec3(size, 1f)
	}

	fun relative(origin: Vec2, size: Vec2){
		components.add(RelativeTransformComponent(this, origin, size))
	}

	fun relative(origin: Vec3, size: Vec2){
		components.add(RelativeTransformComponent(this, origin, size))
	}

	fun pixel(pos: Vec2i, size: Vec2i, origin: Vec2 = Vec2(-1f), screenRelative: Boolean = false){
		components.add(PixelTransformComponent(this, pos, size, origin, screenRelative))
	}
}