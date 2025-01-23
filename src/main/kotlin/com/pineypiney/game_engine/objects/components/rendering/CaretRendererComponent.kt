package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.TextFieldComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class CaretRendererComponent(
	parent: GameObject,
	colour: Vec4,
	shader: Shader = defaultShader,
	shape: Mesh = Mesh.centerSquareShape
) : ColourRendererComponent(parent, colour, shader, shape),
	UpdatingAspectRatioComponent {

	val textField get() = parent.parent?.getComponent<TextFieldComponent>()!!

	override fun render(renderer: RendererI, tickDelta: Double) {
		positionTextAndCaret()
		super.render(renderer, tickDelta)
	}

	private fun positionTextAndCaret() {
		val w = textField.textBox.getComponent<TextRendererComponent>()!!.text.getWidth(0..<textField.caret)
		val ar =
			((textField.parent.getComponent<ShaderRenderedComponent>()!!.shape as Shape2D).size * Vec2(textField.parent.transformComponent.worldScale)).run { y / x }
		val x = (w * ar) + textField.textBox.position.x
		if (x < 0f) {
			textField.textBox.translate(Vec3(-x, 0f, 0f))
			parent.position = Vec3(0f, 0f, 0f)
		} else if (x > 1f) {
			textField.textBox.translate(Vec3(1f - x, 0f, 0f))
			parent.position = Vec3(1f, 0f, 0f)
		} else {
			parent.position = Vec3(x, 0f, .01f)
		}
	}

	override fun updateAspectRatio(renderer: RendererI) {
		positionTextAndCaret()
	}
}