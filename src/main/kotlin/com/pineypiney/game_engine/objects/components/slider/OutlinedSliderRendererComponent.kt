package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec4.Vec4

open class OutlinedSliderRendererComponent(parent: GameObject) : ShaderRenderedComponent(parent, sliderShader) {

	open var colour: Vec4 = Vec4(0.7)
	open var outlineThickness: Float = 0.06f
	open var outlineColour: Vec4 = Vec4(0.5, 0.5, 0.5, 1)

	override val shape: Shape<*> = Mesh.cornerSquareShape.shape

	override fun setUniforms() {
		super.setUniforms()

		uniforms.setVec4Uniform("colour") { colour }
		uniforms.setFloatUniform("outlineThickness", ::outlineThickness)
		uniforms.setVec4Uniform("outlineColour", ::outlineColour)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		val shape = Mesh.cornerSquareShape
		shape.bindAndDraw()
		/*
		// This only updates the stencil if the pixel passes all tests
		//GLFunc.stencilTest = true
		GLFunc.stencilOp = Vec3i(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_REPLACE)
		GLFunc.stencilFRM = Vec3i(GL11C.GL_ALWAYS, 1, 0xFF)
		GLFunc.stencilWriteMask = 0xFF
		shape.bindAndDraw()

		GLFunc.stencilFRM = Vec3i(GL11C.GL_NOTEQUAL, 1, 0xFF)
		GLFunc.stencilWriteMask = 0

		val outline = Vec2(outlineThickness / renderer.aspectRatio, outlineThickness)
		shader.setMat4("model", parent.worldModel.translate(Vec3(-outline, 0f)).scale(Vec3(Vec2(1f, 1f) + (outline * 2))))
		shader.setVec4("colour", outlineColour)
		shape.draw()
		GLFunc.stencilTest = false

		 */
	}

	companion object {
		val sliderShader =
			ShaderLoader.getShader(ResourceKey("vertex/2D_pass_pos"), ResourceKey("fragment/sliders/outlined_slider"))
	}
}