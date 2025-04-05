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

	fun os(origin: Vec2, size: Vec2): MenuItem {
		position = Vec3(origin, 0f)
		scale = Vec3(size, 1f)
		return this
	}

	fun os(origin: Vec3, size: Vec2): MenuItem {
		position = origin
		scale = Vec3(size, 1f)
		return this
	}

	fun relative(origin: Vec2, size: Vec2): MenuItem{
		components.add(RelativeTransformComponent(this, origin, size))
		return this
	}

	fun relative(origin: Vec3, size: Vec2): MenuItem{
		components.add(RelativeTransformComponent(this, origin, size))
		return this
	}

	fun pixel(pos: Vec2i, size: Vec2i, origin: Vec2 = Vec2(-1f), screenRelative: Boolean = false): MenuItem{
		components.add(PixelTransformComponent(this, pos, size, origin, screenRelative))
		return this
	}

	fun pixel(posX: Int, posY: Int, sizeX: Int, sizeY: Int, originX: Float = -1f, originY: Float = -1f, screenRelative: Boolean = false): MenuItem{
		components.add(PixelTransformComponent(this, Vec2i(posX, posY), Vec2i(sizeX, sizeY), Vec2(originX, originY), screenRelative))
		return this
	}
}