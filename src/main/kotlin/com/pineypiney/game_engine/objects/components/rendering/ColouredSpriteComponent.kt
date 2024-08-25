package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec4.Vec4

open class ColouredSpriteComponent(
	parent: GameObject,
	sprite: Sprite,
	var tint: () -> Vec4 = { Vec4(1f, 1f, 1f, 1f) },
	shader: Shader = defaultShader,
	val setUniforms: ColouredSpriteComponent.() -> Unit = {}
) : SpriteComponent(parent, sprite, shader) {

	constructor(
		parent: GameObject,
		texture: Texture,
		pixelsPerUnit: Float = 100f,
		tint: () -> Vec4 = { Vec4(1f, 1f, 1f, 1f) },
		shader: Shader = defaultShader,
		setUniforms: ColouredSpriteComponent.() -> Unit = {}
	) : this(parent, Sprite(texture, pixelsPerUnit), tint, shader, setUniforms)

	constructor(
		parent: GameObject,
		texture: Texture,
		pixelsPerUnit: Float = 100f,
		tint: Vec4 = Vec4(1f, 1f, 1f, 1f),
		shader: Shader = defaultShader,
		setUniforms: ColouredSpriteComponent.() -> Unit = {}
	) : this(parent, Sprite(texture, pixelsPerUnit), { tint }, shader, setUniforms)

	constructor(parent: GameObject) : this(parent, Sprite(Texture.broke, 100f), { Vec4(1f) })

	override val fields: Array<Field<*>> = super.fields + arrayOf(
		Vec4Field("clr", tint) { tint = { it } }
	)

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour") { this.tint() }
		setUniforms.invoke(this)
	}
}