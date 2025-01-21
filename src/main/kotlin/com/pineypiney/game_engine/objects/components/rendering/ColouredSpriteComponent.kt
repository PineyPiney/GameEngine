package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec4.Vec4

open class ColouredSpriteComponent(
	parent: GameObject,
	sprite: Sprite = Sprite(Texture.broke, 100f),
	var tint: Vec4 = Vec4(1f, 1f, 1f, 1f),
	shader: Shader = colouredMenuShader,
	val setUniforms: ColouredSpriteComponent.() -> Unit = {}
) : SpriteComponent(parent, sprite, shader) {

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("tint") { tint }
		setUniforms.invoke(this)
	}

	companion object {
		val colouredMenuShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/texture_coloured")]
	}
}