package com.pineypiney.game_engine.objects.components.fields

import kotlin.reflect.KClass

typealias FieldCreator<T> = (id: String, getter: () -> T, setter: (T) -> Unit, annotations: Set<Annotation>) -> ComponentField<T>

class FieldType<T>(val default: () -> T, val annotations: Set<KClass<out Annotation>>, val fieldCreator: FieldCreator<T>) {

	constructor(default: () -> T, fieldCreator: (id: String, getter: () -> T, setter: (T) -> Unit) -> ComponentField<T>): this(default, emptySet(), { i, g, s, _ -> fieldCreator(i, g, s) })

	override fun toString(): String {
		return "FieldType${fieldCreator}"
	}
}