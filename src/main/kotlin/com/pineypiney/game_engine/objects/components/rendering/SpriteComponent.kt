package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

open class SpriteComponent(
	parent: GameObject,
	sprite: Sprite,
	shader: Shader = defaultShader,
) : ShaderRenderedComponent(parent, shader) {

	var sprite: Sprite = sprite
		set(value) {
			field.mesh.delete()
			field = value
		}

	constructor(parent: GameObject,
				texture: Texture = Texture.broke,
				pixelsPerUnit: Float = 100f,
				shader: Shader = defaultShader,
				spriteCenter: Vec2 = Vec2(.5f)
	): this(parent, Sprite(texture, pixelsPerUnit, spriteCenter), shader)

	constructor(parent: GameObject) : this(parent, Texture.broke)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		sprite.texture.bind()
		sprite.mesh.bindAndDraw()
	}

	override fun getMeshes(): Collection<Mesh> = listOf(sprite.mesh)

	override fun delete() {
		super.delete()
		sprite.mesh.delete()
	}

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/texture"))
		val menuShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/texture")]
	}
}