package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.widgets.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.widgets.TextFieldComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.roundToInt

open class ActionTextField<E : TextFieldComponent>(
	name: String,
	val startText: String = "",
	val textSize: Int = 12,
	val updateType: Int = ActionTextFieldComponent.UPDATE_ON_FINISH,
	val action: (field: E, char: Char, input: Int) -> Unit
) : MenuItem(name) {

	constructor(name: String, origin: Vec3, size: Vec2, startText: String = "", textSize: Int = 12, updateType: Int = ActionTextFieldComponent.UPDATE_ON_FINISH, action: (field: E, char: Char, input: Int) -> Unit): this(name, startText, textSize, updateType, action){
		os(origin, size)
	}

	constructor(name: String, origin: Vec2, size: Vec2, startText: String = "", textSize: Int = 12, updateType: Int = ActionTextFieldComponent.UPDATE_ON_FINISH, action: (field: E, char: Char, input: Int) -> Unit): this(name, startText, textSize, updateType, action){
		os(origin, size)
	}

	constructor(name: String, pos: Vec2i, size: Vec2i, origin: Vec2 = Vec2(-1f), startText: String = "", textString: Int = (size.y * .8f).roundToInt(), updateType: Int = ActionTextFieldComponent.UPDATE_ON_FINISH, action: (E, Char, Int) -> Unit): this(name, startText, textString, updateType, action){
		pixel(pos, size, origin)
	}

	var text: String
		get() = getComponent<TextFieldComponent>()!!.text
		set(value) {
			getComponent<TextFieldComponent>()!!.text = value
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