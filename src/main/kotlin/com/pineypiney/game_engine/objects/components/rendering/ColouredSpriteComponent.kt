package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec4.Vec4

open class ColouredSpriteComponent(
	parent: GameObject,
	sprite: Sprite = Sprite(Texture.broke, 100f),
	var tint: Vec4 = Vec4(1f, 1f, 1f, 1f),
	shader: RenderShader = colouredMenuShader,
	val setUniforms: ColouredSpriteComponent.() -> Unit = {}
) : SpriteComponent(parent, sprite, shader) {

	constructor(parent: GameObject, texture: Texture, ppu: Float, tint: Vec4 = Vec4(1f), shader: RenderShader = colouredMenuShader, setUniforms: ColouredSpriteComponent.() -> Unit = {}):
			this(parent, Sprite(texture, ppu), tint, shader, setUniforms)

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("tint") { tint }
		setUniforms.invoke(this)
	}

	companion object {
		val colouredMenuShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/texture_coloured")]
	}
}