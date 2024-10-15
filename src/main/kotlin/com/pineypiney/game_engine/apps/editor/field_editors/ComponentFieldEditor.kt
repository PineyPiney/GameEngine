package com.pineypiney.game_engine.apps.editor.field_editors

import com.pineypiney.game_engine.objects.components.Component.Field
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import glm_.vec2.Vec2

abstract class ComponentFieldEditor<T, out F : Field<T>>(component: ComponentI, val fullId: String, origin: Vec2, size: Vec2) : MenuItem("$fullId Field Editor") {

	val id = fullId.substringAfterLast('.')
	val field: F = component.getField(removeIDCollectionNumber())
		?: throw Exception("Component $component does not contain field $id}")

	init {
		os(origin, size)
	}

	override fun init() {
		super.init()
		update()
	}

	abstract fun update()

	private fun removeIDCollectionNumber(): String {
		return id.substringBefore('#')
	}
}