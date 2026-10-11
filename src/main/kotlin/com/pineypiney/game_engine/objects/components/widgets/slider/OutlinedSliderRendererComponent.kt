package com.pineypiney.game_engine.objects.components.widgets.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec4.Vec4

open class OutlinedSliderRendererComponent(parent: GameObject) : ShaderRenderedComponent(parent, sliderShader) {

	open var colour: Vec4 = Vec4(0.7)
	open var outlineThickness: Float = 0.005f
	open var outlineColour: Vec4 = Vec4(0.5, 0.5, 0.5, 1)

	val mesh get() = Mesh.centerSquareShape

	override fun setUniforms() {
		super.setUniforms()

		uniforms.setMat4Uniform("model") {
			// Compensate for using centerSquareMesh
			parent.worldModel.translate(.5f, .5f, 0f)
		}
		uniforms.setVec4Uniform("colour", ::colour)
		uniforms.setVec4Uniform("outlineColour", ::outlineColour)
		uniforms.setFloatUniform("width", ::outlineThickness)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		shader.draw("vertexBuffer", mesh, renderer)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	companion object {
		val sliderShader = ShaderLoader.loadRenderShader(ResourceKey("menu/outline_rect"))
	}
}