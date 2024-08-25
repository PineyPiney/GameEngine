package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import glm_.vec2.Vec2

abstract class ScrollListEntryRenderer(parent: GameObject) : SpriteComponent(parent) {

	protected var limits = Vec2(0f)

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec2Uniform("limits", ::limits)
	}
}