package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.TextFieldComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.window.Viewport
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
		positionTextAndCaret(renderer.getViewport())
		super.render(renderer, tickDelta)
	}

	private fun positionTextAndCaret(view: Viewport) {
		val textRenderer = textField.textBox.getComponent<TextRendererComponent>()!!
		// w is the distance from the start of the text to where the caret is relative to half the screens height
		val w = textRenderer.getWidth(textField.text.substring(0..<textField.caret)) * textRenderer.size * 2f / view.size.y

		val width = textField.parent.transformComponent.worldScale.x
		// x is the relative point where the caret should be along the text field
		val x = (w / width) + textField.textBox.position.x
		// If x < 0 then the caret has moved too far left, and the text should be moved back right to put the caret back in the text field
		if (x < 0f) {
			textField.textBox.translate(Vec3(-x, 0f, 0f))
			parent.position = Vec3(0f, 0f, 0f)
		}
		// Else if x > 1 then the caret has moved too far right, and the opposite applies
		else if (x > 1f) {
			textField.textBox.translate(Vec3(1f - x, 0f, 0f))
			parent.position = Vec3(1f, 0f, 0f)
		} else {
			parent.position = Vec3(x, 0f, .01f)
		}
	}

	override fun updateAspectRatio(view: Viewport) {
		positionTextAndCaret(view)
	}
}