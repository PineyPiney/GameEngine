package com.pineypiney.game_engine.apps.editor.field_editors

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.Component.*
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4


open class VecFieldEditor<T, out F : Field<T>>(
	parent: GameObject,
	component: ComponentI,
	id: String,
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit,
	vecSize: Int,
	copy: (T) -> T,
	val inGet: T.(Int) -> Float,
	inSet: T.(Int, Float) -> Unit
) : FieldEditor<T, F>(parent, component, id, origin, size) {

	val textFields = Array<ActionTextField<*>>(vecSize) {
		ActionTextField<ActionTextFieldComponent<*>>(
			"Vec Field $it",
			Vec3((size.x * it / vecSize), 0f, .01f),
			Vec2(size.x / vecSize, 1f)
		) { f, _, _ ->
			try {
				val newVal = copy(field.getter())
				newVal.inSet(it, java.lang.Float.parseFloat(f.text))
				field.setter(newVal)
				callback(fullId, field.serialise(newVal))
			} catch (_: NumberFormatException) {

			}
		}.also { f -> f.name = "${parent.name.substring(0, parent.name.length - 13)}.${names[it]} Field Editor" }
	}

	override fun createChildren() {
		parent.addChild(*textFields)
	}

	override fun update() {
		val v = field.getter()
		textFields.forEachIndexed { index, actionTextField ->
			actionTextField.text = v.inGet(index).toString()
		}
	}

	companion object {
		val names = charArrayOf('x', 'y', 'z', 'w')
	}
}

open class Vec2FieldEditor(
	parent: GameObject,
	component: ComponentI,
	id: String,
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Vec2, Vec2Field>(parent, component, id, origin, size, callback, 2, ::Vec2, Vec2::get, Vec2::set)

open class Vec3FieldEditor(
	parent: GameObject,
	component: ComponentI,
	id: String,
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Vec3, Vec3Field>(parent, component, id, origin, size, callback, 3, ::Vec3, Vec3::get, Vec3::set)

open class Vec4FieldEditor(
	parent: GameObject,
	component: ComponentI,
	id: String,
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Vec4, Vec4Field>(parent, component, id, origin, size, callback, 4, ::Vec4, Vec4::get, Vec4::set)

open class QuatFieldEditor(
	parent: GameObject,
	component: ComponentI,
	id: String,
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Quat, QuatField>(parent, component, id, origin, size, callback, 4, ::Quat, Quat::get, Quat::set)


open class OldQuatFieldEditor(parent: GameObject, component: ComponentI, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) : FieldEditor<Quat, QuatField>(parent, component, id, origin, size) {

	val xField = ActionTextField<ActionTextFieldComponent<*>>("Quat X Field", Vec3(0f, 0f, .01f), Vec2(size.x * 0.25f, 1f)) { f, _, _ ->
		try {
			val v = field.getter()
			val newVal = Quat(v.w, java.lang.Float.parseFloat(f.text), v.y, v.z)
			field.setter(newVal)
			callback(fullId, field.serialise(newVal))
		} catch (_: NumberFormatException) {

		}
	}

	val yField = ActionTextField<ActionTextFieldComponent<*>>(
		"Quat Y Field",
		Vec3(size.x * 0.25f, 0f, .01f),
		Vec2(size.x * 0.25f, 1f)
	) { f, _, _ ->
		try {
			val v = field.getter()
			v[0]
			val newVal = Quat(v.w, v.x, java.lang.Float.parseFloat(f.text), v.z)
			field.setter(newVal)
			callback(fullId, field.serialise(newVal))
		} catch (_: NumberFormatException) {

		}
	}

	val zField =
		ActionTextField<ActionTextFieldComponent<*>>("Quat Z Field", Vec3(size.x * 0.5f, 0f, .01f), Vec2(size.x * 0.25f, 1f)) { f, _, _ ->
			try {
				val v = field.getter()
				val newVal = Quat(v.w, v.x, v.y, java.lang.Float.parseFloat(f.text))
				field.setter(newVal)
				callback(fullId, field.serialise(newVal))
			} catch (_: NumberFormatException) {

			}
		}

	val wField = ActionTextField<ActionTextFieldComponent<*>>(
		"Quat W Field",
		Vec2(size.x * 0.75f, 0f),
		Vec2(size.x * 0.25f, 1f)
	) { f, _, _ ->
		try {
			val v = field.getter()
			val newVal = Quat(java.lang.Float.parseFloat(f.text), v.x, v.y, v.z)
			field.setter(newVal)
			callback(fullId, field.serialise(newVal))
		} catch (_: NumberFormatException) {

		}
	}

	val textFields = arrayOf(xField, yField, zField, wField)

	override fun createChildren() {
		parent.addChild(*textFields)
	}

	override fun update() {
		val v = field.getter()
		xField.text = v.x.toString()
		yField.text = v.y.toString()
		zField.text = v.z.toString()
		wField.text = v.w.toString()
	}
}
