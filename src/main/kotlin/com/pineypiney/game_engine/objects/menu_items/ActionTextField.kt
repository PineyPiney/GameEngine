package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.TextFieldComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import glm_.vec2.Vec2
import glm_.vec4.Vec4

open class ActionTextField<E : TextFieldComponent>(
	name: String,
	origin: Vec2,
	size: Vec2,
	val startText: String = "",
	val textSize: Float = 1f,
	val updateType: Int = ActionTextFieldComponent.UPDATE_ON_FINISH,
	val action: (field: E, char: Char, input: Int) -> Unit
) : MenuItem(name) {

	constructor(origin: Vec2, size: Vec2, action: (field: E, char: Char, input: Int) -> Unit): this("Action Text Field", origin, size, action = action)

	var text: String
		get() = getComponent<TextFieldComponent>()!!.text
		set(value) {
			getComponent<TextFieldComponent>()!!.text = value
		}

	init {
		os(origin, size)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(ActionTextFieldComponent(this, startText, textSize, updateType, action))
		components.add(
			ColourRendererComponent(
				this,
				Vec4(0.5f, .5f, .5f, 1f),
				ColourRendererComponent.menuShader,
				Mesh.cornerSquareShape
			)
		)
	}
}