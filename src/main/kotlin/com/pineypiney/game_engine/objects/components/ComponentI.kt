package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.components.fields.ComponentField

interface ComponentI : Initialisable {

	val id: String
	val parent: GameObject

	fun getAllFields(): Set<ComponentField<*>>

	fun setValue(key: String, value: String)

	fun <F : ComponentField<*>> getField(id: String): F?

	@Throws(InstantiationError::class)
	fun copy(newParent: GameObject): ComponentI

	fun copyFieldsTo(dst: ComponentI)

	fun <T> copyFieldTo(dst: Collection<ComponentField<*>>, field: ComponentField<T>)

	fun <T> getMatchingField(other: Collection<ComponentField<*>>, field: ComponentField<T>): ComponentField<T>?

	fun serialise(head: StringBuilder, data: StringBuilder)
}