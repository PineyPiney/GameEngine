package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2

open class SpriteComponent(
	parent: GameObject,
	var sprite: Sprite,
	shader: Shader = defaultShader,
) : ShaderRenderedComponent(parent, shader) {

	constructor(parent: GameObject,
				texture: Texture = Texture.broke,
				pixelsPerUnit: Float = 100f,
				shader: Shader = defaultShader,
				spriteCenter: Vec2 = Vec2(.5f)
	): this(parent, Sprite(texture, pixelsPerUnit, spriteCenter), shader)

	override val renderSize: Vec2 get() = Vec2(sprite.renderWidth, sprite.renderHeight)

	override val shape: Shape<*> get() = sprite.mesh.shape

	constructor(parent: GameObject) : this(parent, Texture.broke)

	override val fields: Array<Field<*>> = arrayOf(
		Field(
			"txr",
			::DefaultFieldEditor,
			::sprite,
			{ this.sprite = it },
			{ it.texture.fileLocation.substringBefore('.') },
			{ _, s -> Sprite(TextureLoader[ResourceKey(s)], sprite.pixelsPerUnit) }
		),
		FloatField("ppu", sprite::pixelsPerUnit) { this.sprite.pixelsPerUnit = it }
	)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		sprite.texture.bind()
		sprite.mesh.bindAndDraw()
	}

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/texture"))
		val menuShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/texture")]
	}
}