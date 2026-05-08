package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.objects.components.fields.EditorIgnore
import com.pineypiney.game_engine.util.exceptions.ComponentReflectionException
import com.pineypiney.game_engine.util.extension_functions.string
import com.pineypiney.game_engine.util.serialisation.SerialOps
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

abstract class Component(final override val parent: GameObject) : ComponentI {

	override val id: String get() = this::class.simpleName ?: "Anon"

	override fun init() {

	}

	override fun getAllFields(): Set<ComponentField<*>> {
		return getAllFieldsExt()
	}

	override fun setValue(key: String, value: String) {
		val field = getAllFieldsExt().firstOrNull { it.id == key } ?: return
		field.set(value)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <F : ComponentField<*>> getField(id: String): F? {
		val f = getAllFieldsExt().firstOrNull { it.id == id } ?: return null
		return f as? F
	}

	@Throws(InstantiationError::class)
	override fun copy(newParent: GameObject): Component {
		val clazz = this::class
		val constructors = clazz.constructors

		val oClass = GameObject::class.java
		val smallConst =
			constructors.firstOrNull { it.parameters.size == 1 && it.parameters[0].type.javaType == oClass }

		val newComponent: Component =
			// If there is a small Constructor that just takes a GameObject then use that
			if (smallConst != null) smallConst.call(newParent)
			else {
				// Otherwise use the primary constructor or the first one
				val params = mutableMapOf<KParameter, Any?>()
				var func: KFunction<Component>? = null
				var i = 0
				val errors = mutableListOf<String>()
				constructors@ for (constructor in constructors) {

					for (param in constructor.parameters) {
						// If this is a GameObject parameter set the new parent
						if (param.type.javaType == oClass) params[param] = newParent
						else if (param.isOptional) continue
						else {
							// Search for a member property with the same name and type
							val memberProperty = clazz.memberProperties.firstOrNull { it.name == param.name }
							var good = true

							if (memberProperty == null) {
								errors.add("Constructor ${i++} invalid, param ${param.name} does not have a matching class member")
								good = false
							} else if (!memberProperty.returnType.isSupertypeOf(param.type)) {
								errors.add("Constructor ${i++} invalid, param ${param.name} type is ${param.type}, matching field type is ${memberProperty.returnType}")
								good = false
							}
							if (good) {
								try {
									params[param] = memberProperty?.call(this)
									continue
								} catch (_: IllegalCallableAccessException) {
									errors.add("Constructor ${i++} invalid, param ${param.name} matching field is inaccessible")
								}
							}
							params.clear()
							continue@constructors
						}
					}
					// Managed to fill out all the parameters
					func = constructor
					break
				}

				func?.callBy(params) ?: throw InstantiationError(
					"Could not copy Component Class $clazz, did not have a default constructor and could not use any of the available constructors for the following reasons:\n" + errors.joinToString(
						"\n"
					)
				)
			}

		copyFieldsTo(newComponent)
		return newComponent
	}

	override fun copyFieldsTo(dst: ComponentI) {
		for (f in getAllFieldsExt()) {
			copyFieldTo(dst.getAllFieldsExt(), f)
		}
	}

	override fun <T> copyFieldTo(dst: Collection<ComponentField<*>>, field: ComponentField<T>) {
		try{
			val dstField = getMatchingField(dst, field)
			field.copyTo(dstField)
		}
		catch(e: ComponentReflectionException){
			println(e.message)
			e.printStackTrace()
			return
		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getMatchingField(other: Collection<ComponentField<*>>, field: ComponentField<T>): ComponentField<T> {
		val matchingField = other.firstOrNull { it.id == field.id }
		if(matchingField == null) throw ComponentReflectionException("Could not find field with id ${field.id}")
		return try {
			matchingField as ComponentField<T>
		} catch (e: ClassCastException) {
			throw ComponentReflectionException("Couldn't cast $matchingField to ${field::class}, it's type was ${matchingField::class}", e)
		}
	}

	override fun delete() {

	}

	override fun toString(): String {
		return "Component[${this::class.simpleName}]"
	}

	override fun <E> encode(ops: SerialOps<E>): E {
		val nameStr = this::class.simpleName ?: "Anon"
		val properties = getAllFieldsExt()
		val componentMap = ops.createMap()
		ops.put(componentMap, "name", nameStr)
		properties.forEach { ops.appendMap(componentMap, it.id, it.encode(ops)) }
		return componentMap
	}

	override fun <E> decode(ops: SerialOps<E>, head: E, lateParse: LateParse<E>) {
		val fields = getAllFieldsExt()
		ops.forEachEntry(head) { (k, v) ->
			if (k != "name") {
				val field = fields.firstOrNull { it.id == k } ?: return@forEachEntry
				field.decode(ops, v, this, lateParse)
			}
		}
	}

	override fun encode(stream: OutputStream) {
		val nameStr = this::class.simpleName ?: "Anon"
		val fields = getAllFieldsExt()
		stream.write(nameStr.length)
		stream.string(nameStr)
		stream.write(fields.size)
		fields.forEach { it.serialise(stream) }
	}

	override fun decode(stream: InputStream, lateParse: LateParse<ByteArray>) {
		val fields = getAllFieldsExt()
		val numFields = stream.read()
		repeat(numFields) {
			val fieldNameSize = stream.read()
			val fieldName = stream.string(fieldNameSize)
			val field = fields.firstOrNull { it.id == fieldName } ?: return@repeat
			field.parse(stream, this, lateParse)
		}
	}
}

fun <C : Component> C.applied(): C {
	parent.components.add(this)
	return this
}

fun <C: Any> C.getAllFieldsExt(parent: String = ""): Set<ComponentField<*>> {
	val properties = this::class.memberProperties.filterIsInstance<KMutableProperty1<C, Any>>()
	val fields = mutableSetOf<ComponentField<*>>()
	for(p in properties){
		p.getFieldExt(parent, this, fields)
	}
	return fields
}

fun <C : Any, T> KMutableProperty1<C, T>.getFieldExt(parent: String, container: C, fields: MutableSet<ComponentField<*>>) {
	if(visibility != KVisibility.PUBLIC || setter.visibility != KVisibility.PUBLIC || hasAnnotation<EditorIgnore>()) return
	val field = Components.getDefaultField(this, container, parent)
	if(field != null) fields.add(field)
	else {
		val value = get(container) ?: return
		fields.addAll(value.getAllFieldsExt("$parent$name."))
	}
}
