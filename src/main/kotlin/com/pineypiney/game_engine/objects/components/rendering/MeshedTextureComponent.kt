package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.util.ResourceKey

open class MeshedTextureComponent(
	parent: GameObject,
	var texture: Texture2D = Texture2D.missing,
	shader: RenderShader = default2DShader,
	val mesh: Mesh = Mesh.centerSquareShape
) : ShaderRenderedComponent(parent, shader) {

	constructor(parent: GameObject) : this(parent, Texture2D.missing)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		texture.bind()
		mesh.bindAndDraw(renderer.getRenderingApi())
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	companion object {
		val default2DShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/texture"))
		val default3DShader = ShaderLoader.getShader(ResourceKey("vertex/3D"), ResourceKey("fragment/texture"))
	}
}