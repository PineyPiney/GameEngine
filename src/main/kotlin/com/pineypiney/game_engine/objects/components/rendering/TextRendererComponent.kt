package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

open class TextRendererComponent(parent: GameObject, val text: Text, shader: Shader) :
	ShaderRenderedComponent(parent, shader), UpdatingAspectRatioComponent, PreRenderComponent {

	override val whenVisible: Boolean = true
	override val shape: Rect2D get() = Rect2D(Vec2(), Vec2(text.getWidth(), text.getHeight()))

		override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4Uniform("model"){ parent.worldModel.let { it.scale(text.size / it[0, 0], text.size / it[1, 1], 1f) } }
		uniforms.setVec4Uniform("colour", text::colour)
		uniforms.setFloatUniform("italic", text::italic)
	}

	override fun init() {
		super.init()
		text.init()
	}

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		if (text.textChanged) {
			text.updateLines(Vec2(parent.transformComponent.worldScale))
			text.textChanged = false
		}
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		if (text.lines.isEmpty()) return


		shader.use()
		shader.setUniforms(uniforms, renderer)
		text.mesh.texture.bind()
		text.mesh.bindAndDraw()
	}

	fun renderUnderline(
		model: Mat4,
		renderer: RendererI,
		line: String = text.text,
		amount: Float = text.underlineAmount
	) {
		val shader = if (shader.hasView) ColourRendererComponent.defaultShader else ColourRendererComponent.menuShader
		val newModel = model.scale(text.font.getWidth(line) * amount, text.underlineThickness, 0f)
			.translate(0f, text.underlineOffset, 0f)

		shader.use()
		if (shader.hasView) shader.setVP(renderer)
		shader.setMat4("model", newModel)
		shader.setVec4("colour", text.colour)
		Mesh.cornerSquareShape.bindAndDraw()
	}

	fun getFormattedOrigin(w: Float, h: Float): Mat4 {
		val o = Mat4(parent.worldModel)
		val a = text.alignment
		val bw = 1f - w
		val bh = 1f - h

		if (text.alignment == Text.ALIGN_BOTTOM_LEFT) return o

		when (a and 0xf) {
			Text.ALIGN_CENTER_H -> o.translateAssign(bw * 0.5f, 0f, 0f)
			Text.ALIGN_RIGHT -> o.translateAssign(bw, 0f, 0f)
		}
		when (a and 0xf0) {
			Text.ALIGN_CENTER_V -> o.translateAssign(0f, bh * 0.5f, 0f)
			Text.ALIGN_TOP -> o.translateAssign(0f, bh, 0f)
		}

		return o
	}

	override fun updateAspectRatio(renderer: RendererI) {
		if (!shader.hasProj) text.updateLines(Vec2(parent.transformComponent.worldScale))
	}

	override fun delete() {
		super.delete()
		text.delete()
	}

	companion object {
		val gameTextShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/text")]
	}
}