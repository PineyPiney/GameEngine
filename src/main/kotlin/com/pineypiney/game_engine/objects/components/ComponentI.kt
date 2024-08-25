package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.Initialisable
import glm_.*

interface ComponentI : Initialisable {

	val parent: GameObject
	val id: String

	val fields: Array<Component.Field<*>>

	fun getAllFields(): Array<Component.Field<*>>

	fun setValue(key: String, value: String)

	fun <F : Component.Field<*>> getField(id: String): F?

	@Throws(InstantiationError::class)
	fun copy(newParent: GameObject): ComponentI

	fun copyFieldsTo(dst: ComponentI)

	fun <T> copyFieldTo(dst: ComponentI, field: Component.Field<T>)

	fun <T> getMatchingField(other: ComponentI, field: Component.Field<T>): Component.Field<T>?

	fun serialise(head: StringBuilder, data: StringBuilder)
}