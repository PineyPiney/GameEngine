package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec4.Vec4

open class ColourSliderRendererComponent(parent: GameObject, shader: Shader, val colours: MutableMap<String, Float>) :
	ShaderRenderedComponent(parent, shader) {

	val mesh = Mesh.cornerSquareShape

	override fun setUniforms() {
		super.setUniforms()

		uniforms.setFloatUniform("outlineThickness") { 0.005f }
		uniforms.setVec4Uniform("outlineColour") { Vec4(0, 0, 0, 1) }
		for ((colour, _) in colours) {
			uniforms.setFloatUniform(colour) { colours[colour] ?: 0f }
		}
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		mesh.bindAndDraw()
	}

	override fun getScreenShape(): Shape<*> {
		return mesh.shape
	}

	operator fun get(colour: String) = colours[colour] ?: 0f
	operator fun set(colour: String, value: Float) {
		colours[colour] = value
	}

	companion object {
		val redShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/red_slider")))
		val greenShader =
			ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/green_slider")))
		val blueShader =
			ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey(("fragment/sliders/blue_slider")))
	}
}