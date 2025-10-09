package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2t
import glm_.vec4.Vec4

class BezierDisplay(parent: GameObject, val points: Array<Vec2t<*>>) : ShaderRenderedComponent(
	parent, when (points.size) {
		4 -> b3Shader
		3 -> b2Shader
		else -> b1Shader
	}
) {

	val mesh = Mesh.cornerSquareShape

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec2sUniform("points") { points.asList() }
		uniforms.setVec4Uniform("colour") { Vec4(1) }
		uniforms.setFloatUniform("width") { 0.0002f }
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		mesh.bindAndDraw()
	}

	companion object {
		val b1Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier1")]
		val b2Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier2")]
		val b3Shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/bezier3")]
	}
}