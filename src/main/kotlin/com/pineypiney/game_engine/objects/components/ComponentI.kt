package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.util.serialisation.SerialOps
import java.io.InputStream
import java.io.OutputStream

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

	fun <E> encode(ops: SerialOps<E>): E
	fun <E> decode(ops: SerialOps<E>, head: E, lateParse: LateParse<E>)
	fun encode(stream: OutputStream)
	fun decode(stream: InputStream, lateParse: LateParse<ByteArray>)
}