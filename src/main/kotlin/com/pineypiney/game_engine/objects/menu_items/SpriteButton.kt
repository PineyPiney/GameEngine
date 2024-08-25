package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
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
	val shader: Shader = SpriteComponent.menuShader,
	override val action: (ButtonComponent, Vec2) -> Unit
) : AbstractButton(name) {

	constructor(
		name: String,
		icon: Texture,
		ppu: Float,
		origin: Vec3 = Vec3(0f),
		size: Vec2 = Vec2(1f),
		shader: Shader = SpriteComponent.menuShader,
		action: (ButtonComponent, Vec2) -> Unit
	): this(name, Sprite(icon, ppu), origin, size, shader, action)

	var baseTint = Vec4(1f)
	var hoverTint = Vec4(.95f)
	var clickTint = Vec4(.9f)

	init {
		os(origin, size)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(ColouredSpriteComponent(this, sprite, ::selectColour, shader))
	}

	fun selectColour(): Vec4 {
		return when {
			getComponent<ButtonComponent>()!!.pressed -> clickTint
			getComponent<ButtonComponent>()!!.hover -> hoverTint
			else -> baseTint
		}
	}
}