package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.RelativeTransformComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.util.extension_functions.fromHex
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class TextButton(
	val string: String,
	origin: Vec2,
	size: Vec2,
	relative: Boolean = false,
	textColour: Vec4 = Vec4(0, 0, 0, 1),
	action: (button: ButtonComponent, cursorPos: Vec2) -> Unit
) : MenuItem("$string Button") {

	//constructor(string: String, origin: Vec2, size: Vec2, textColour: Vec4 = Vec4(0f, 0f, 0f, 1f), action: () -> Unit): this(string, origin, size, textColour, { _, _ -> action() })

	var textObject = Text.makeMenuText(string, textColour, 1f, 1f, 0f, Text.ALIGN_CENTER)

	var baseColour = Vec4.fromHex(0x00BFFF)
	var hoverColour = Vec4.fromHex(0x008CFF)
	var clickColour = Vec4.fromHex(0x026FFF)

	val action: (button: ButtonComponent, cursorPos: Vec2) -> Unit = { b, c ->
		action(b, c)
		setColour(b, c)
	}

	init {
		if(relative) components.add(RelativeTransformComponent(this, origin, size))
		else os(origin, size)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(
			ColourRendererComponent(
				this,
				baseColour,
				ColourRendererComponent.menuShader,
				Mesh.cornerSquareShape
			)
		)
		components.add(ButtonComponent(this, action, ::setColour, ::setColour, ::setColour))
	}

	override fun addChildren() {
		super.addChildren()
		addChild(textObject)
		textObject.translate(Vec3(0f, 0f, .01f))
	}

	fun selectColour(): Vec4 {
		val but = getComponent<ButtonComponent>() ?: return baseColour
		return when {
			but.pressed -> clickColour
			but.hover -> hoverColour
			else -> baseColour
		}
	}

	open fun setColour(button: ButtonComponent, cursorPos: Vec2) {
		getComponent<ColourRendererComponent>()?.colour = selectColour()
	}

	open fun setColour(button: ButtonComponent, cursorPos: Vec2, cursorDelta: Vec2) {
		getComponent<ColourRendererComponent>()?.colour = selectColour()
	}
}