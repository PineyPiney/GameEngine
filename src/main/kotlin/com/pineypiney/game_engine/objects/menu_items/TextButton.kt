package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.widgets.ButtonComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.extension_functions.fromHex
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4

open class TextButton(
	val string: String,
	textColour: Vec4 = Vec4(0, 0, 0, 1),
	action: (button: ButtonComponent, cursorPos: Vec2) -> Unit
) : MenuItem("$string Button") {

	constructor(
		string: String,
		origin: Vec2,
		size: Vec2,
		relative: Boolean = false,
		textColour: Vec4 = Vec4(0, 0, 0, 1),
		action: (button: ButtonComponent, cursorPos: Vec2) -> Unit
	): this(string, textColour, action){
		if(relative) relative(origin, size)
		else os(origin, size)
	}

	constructor(string: String, origin: Vec2, size: Vec2, relative: Boolean = false, textColour: Vec4 = Vec4(0f, 0f, 0f, 1f), action: () -> Unit): this(string, origin, size, relative, textColour, { _, _ -> action() })

	constructor(string: String, pos: Vec2i, size: Vec2i, origin: Vec2 = Vec2(-1f),
				screenRelative: Boolean = false, textColour: Vec4 = Vec4(0f, 0f, 0f, 1f),
				action: (button: ButtonComponent, cursorPos: Vec2) -> Unit
	): this(string, textColour, action){
		pixel(pos, size, origin, screenRelative)
	}

	constructor(string: String, pos: Vec2i, size: Vec2i, origin: Vec2 = Vec2(-1f),
				screenRelative: Boolean = false, textColour: Vec4 = Vec4(0f, 0f, 0f, 1f),
				action: () -> Unit
	): this(string, pos, size, origin, screenRelative, textColour, { _, _ -> action() })

	var textObject = Text.makeMenuText(string, textColour, 0, Text.ALIGN_CENTER)

	var baseColour = Vec4.fromHex(0x00BFFF)
	var hoverColour = Vec4.fromHex(0x008CFF)
	var clickColour = Vec4.fromHex(0x026FFF)

	val action: (button: ButtonComponent, cursorPos: Vec2) -> Unit = { b, c ->
		action(b, c)
		setColour()
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
		components.add(ButtonComponent(this, action, { _, _ -> setColour() }, { _, _, _ -> setColour() }, { _, _, _ -> setColour() }))
	}

	override fun addChildren() {
		super.addChildren()
		addChild(textObject)
	}

	fun selectColour(): Vec4 {
		val but = getComponent<ButtonComponent>() ?: return baseColour
		return when {
			but.pressed -> clickColour
			but.hover -> hoverColour
			else -> baseColour
		}
	}

	open fun setColour() {
		getComponent<ColourRendererComponent>()?.colour = selectColour()
	}
}