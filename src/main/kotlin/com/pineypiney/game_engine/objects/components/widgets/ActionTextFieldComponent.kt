package com.pineypiney.game_engine.objects.components.widgets

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.input.InputState
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.roundToInt

open class ActionTextFieldComponent<E : TextFieldComponent>(
	parent: GameObject,
	startText: String,
	textSize: Int = 12,
	val updateType: Int = UPDATE_ON_FINISH,
	val action: TextFieldAction<E>
) : TextFieldComponent(parent, startText, textSize) {

	override fun type(char: Char) {
		super.type(char)
		@Suppress("UNCHECKED_CAST")
		if (updateType == UPDATE_EVERY_CHAR) action(this as E, char, char.i)
	}

	override fun specialCharacter(bind: InputState) {
		super.specialCharacter(bind)
		@Suppress("UNCHECKED_CAST")
		if (standard.contains(bind.c) && updateType == UPDATE_EVERY_CHAR) action(this as E, bind.c, bind.i)
	}

	override fun finish() {
		super.finish()
		@Suppress("UNCHECKED_CAST")
		if (updateType == UPDATE_ON_FINISH) action(this as E, ' ', 0)
	}

	companion object {
		const val UPDATE_EVERY_CHAR = 1
		const val UPDATE_ON_FINISH = 2

		fun <E : TextFieldComponent> createActionTextField(
			name: String,
			startText: String = "",
			textSize: Int = 12,
			updateType: Int = UPDATE_ON_FINISH,
			action: TextFieldAction<E>
		): Pair<GameObject, ActionTextFieldComponent<E>> {
			val obj = GameObject(name, 1)
			return obj to addActionTextField(obj, startText, updateType, textSize, action)
		}

		fun <E : TextFieldComponent> createActionTextFieldAt(name: String, origin: Vec2, size: Vec2, updateType: Int = UPDATE_ON_FINISH, action: TextFieldAction<E>): ActionTextFieldComponent<E> {
			val obj = GameObject(name, 1)
			obj.os(origin, size)
			return addActionTextField(obj, "", updateType, 12, action)
		}

		fun <E : TextFieldComponent> createActionTextFieldAt(
			name: String,
			origin: Vec3,
			size: Vec2,
			startText: String,
			textSize: Int,
			updateType: Int = UPDATE_ON_FINISH,
			action: TextFieldAction<E>
		): ActionTextFieldComponent<E> {
			val obj = GameObject(name, 1)
			obj.os(origin, size)
			return addActionTextField(obj, startText, updateType, textSize, action)
		}

		fun <E : TextFieldComponent> createActionTextFieldAtPixel(
			name: String,
			pos: Vec2i,
			size: Vec2i,
			origin: Vec2 = Vec2(-1f),
			startText: String = "",
			textSize: Int = (size.y * .8f).roundToInt(),
			updateType: Int = UPDATE_ON_FINISH,
			action: TextFieldAction<E>
		): ActionTextFieldComponent<E> {
			val obj = GameObject(name, 1)
			obj.pixel(pos, size, origin)
			return addActionTextField(obj, startText, updateType, textSize, action)
		}

		fun <E : TextFieldComponent> addActionTextField(obj: GameObject, startText: String, updateType: Int, textSize: Int, action: TextFieldAction<E>): ActionTextFieldComponent<E> {
			val comp = ActionTextFieldComponent(obj, startText, textSize, updateType, action)
			obj.components.addAll(
				comp,
				ColourRendererComponent(
					obj,
					Vec4(0.5f, .5f, .5f, 1f),
					ColourRendererComponent.menuShader,
					Mesh.cornerSquareShape
				)
			)
			return comp
		}
	}
}

typealias TextFieldAction<E> = (field: E, char: Char, input: Int) -> Unit