package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.Viewport
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import kotlin.math.ceil
import kotlin.math.min

open class TextRendererComponent(parent: GameObject, protected val text: Text, var fontSize: Int, shader: RenderShader) :
	ShaderRenderedComponent(parent, shader), UpdatingAspectRatioComponent, PreRenderComponent {

	// Initialise as true so the text is generated before it's first render call
	var textChanged = true
	override val whenVisible: Boolean = true

	var size: Int = 12

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4UniformR("model"){ renderer ->
			val renderSize = 2f * size.toFloat() / renderer.viewportSize.y
			parent.worldModel.let { it.scale(renderSize / it[0, 0], renderSize / it[1, 1], 1f) }
		}
		uniforms.setVec4Uniform("colour", text::colour)
		uniforms.setFloatUniform("italic", text::italic)
	}

	override fun init() {
		super.init()
		text.init()
	}

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		if (textChanged) {
			updateLines(renderer.getViewport())
			textChanged = false
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

	fun getScreenScale(renderer: RendererI) = size * 2f / renderer.viewportSize.y

	fun getRenderSize(renderer: RendererI): Vec2{
		val scale = getScreenScale(renderer)
		return Vec2(text.getWidth() * scale, text.getHeight() * scale)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(text.mesh)

	fun getTextContent() = text.text

	fun setTextContent(newText: String){
		text.text = newText
		textChanged = true
	}

	fun setTextColour(colour: Vec4){
		text.colour(colour)
	}

	fun setAlignment(alignment: Int){
		text.alignment = alignment
		textChanged = true
	}

	fun getWidth(text: String) = this.text.getWidth(text)
	fun getWidth(): Float = text.lengths.maxOrNull() ?: text.lines.maxOfOrNull { text.getWidth(it) } ?: 0f
	fun getHeight() = text.getHeight()

	fun setUnderlineThickness(thickness: Float){
		text.underlineThickness = thickness
	}

	fun getUnderlineAmount() = text.underlineAmount

	fun setUnderlineAmount(amount: Float){
		text.underlineAmount = amount
	}

	fun fitWithin(view: Viewport, bounds: Vec2) {
		val fSize = text.font.getSize(text.text)
		val fits = bounds / fSize
		val minScreenSpace = min(fits.x, fits.y)
		size = (minScreenSpace * view.size.y * .5f).toInt()
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

	fun updateLines(view: Viewport){
		val size = Vec2(parent.transformComponent.worldScale)
		if (fontSize > 0f) this.size = fontSize
		else fitWithin(view, size)
		val pixelSize = Vec2(ceil(size.x * view.size.y), ceil(size.y * view.size.y))
		text.updateLines(pixelSize / (this.size * 2f))
	}

	override fun updateAspectRatio(view: Viewport) {
		if (!shader.hasProj) updateLines(view)
	}

	override fun delete() {
		super.delete()
		text.delete()
	}

	companion object {
		val gameTextShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/text")]
	}
}