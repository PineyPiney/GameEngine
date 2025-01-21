package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class SpriteButton(
	name: String,
	val sprite: Sprite,
	origin: Vec3 = Vec3(0f),
	size: Vec2 = Vec2(1f),
	val shader: Shader = ColouredSpriteComponent.colouredMenuShader,
	val action: (ButtonComponent, Vec2) -> Unit
) : MenuItem(name) {

	constructor(
		name: String,
		icon: Texture,
		ppu: Float,
		origin: Vec3 = Vec3(0f),
		size: Vec2 = Vec2(1f),
		shader: Shader = ColouredSpriteComponent.colouredMenuShader,
		action: (ButtonComponent, Vec2) -> Unit
	): this(name, Sprite(icon, ppu), origin, size, shader, action)

	var baseTint = Vec4(1f)
	var hoverTint = Vec4(.95f)
	var clickTint = Vec4(.9f)
	val tint = baseTint

	init {
		os(origin, size)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(ButtonComponent(this, { b, v -> action(b, v); tint.put(selectColour()) }, { b, v -> tint.put(selectColour())}, { _, _, _ -> tint.put(selectColour())}, { _, _, _ -> tint.put(selectColour())}))
		components.add(ColouredSpriteComponent(this, sprite, tint, shader))
	}

	fun selectColour(): Vec4 {
		return when {
			getComponent<ButtonComponent>()!!.pressed -> clickTint
			getComponent<ButtonComponent>()!!.hover -> hoverTint
			else -> baseTint
		}
	}
}