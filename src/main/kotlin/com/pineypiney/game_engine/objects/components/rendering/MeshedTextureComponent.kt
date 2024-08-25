package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2

open class MeshedTextureComponent(
	parent: GameObject,
	var texture: Texture = Texture.broke,
	shader: Shader = default2DShader,
	val vShape: Mesh = Mesh.centerSquareShape
) : ShaderRenderedComponent(parent, shader) {

	override val renderSize: Vec2 get() = Vec2(1f)

	override val shape: Shape<*> = vShape.shape

	constructor(parent: GameObject) : this(parent, Texture.broke)

	override val fields: Array<Field<*>> = arrayOf(
		Field(
			"txr",
			::DefaultFieldEditor,
			::texture,
			{ texture = it },
			{ it.fileLocation.substringBefore('.') },
			{ _, s -> TextureLoader[ResourceKey(s)] })
	)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		texture.bind()
		vShape.bindAndDraw()
	}

	companion object {
		val default2DShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/texture"))
		val default3DShader = ShaderLoader.getShader(ResourceKey("vertex/3D"), ResourceKey("fragment/texture"))
	}
}