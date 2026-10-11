package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class ColourRendererComponent(parent: GameObject, var colour: Vec4 = Vec4(1f), shader: RenderShader = shader3D, val mesh: Mesh = Mesh.centerSquareShape) :
	ShaderRenderedComponent(parent, shader) {

	constructor(parent: GameObject, colour: Vec3, shader: RenderShader = shader3D, mesh: Mesh = Mesh.centerSquareShape) : this(parent, Vec4(colour, 1f), shader, mesh)

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour", ::colour)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		shader.draw("vertexBuffer", mesh, renderer)
	}

	companion object {
		val shader3D = ShaderLoader.loadRenderShader(ResourceKey("3d/colour"))
		val shader2D = ShaderLoader.loadRenderShader(ResourceKey("2d/colour"))
		val vertexColours = ShaderLoader.loadRenderShader(ResourceKey("3d/vertex_colour"))
		val menuShader = ShaderLoader.loadRenderShader(ResourceKey("menu/colour"))
	}
}