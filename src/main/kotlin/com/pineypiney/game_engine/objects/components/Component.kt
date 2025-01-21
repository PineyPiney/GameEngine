package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.objects.components.fields.EditorIgnore
import com.pineypiney.game_engine.util.extension_functions.toByteString
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

abstract class Component(final override val parent: GameObject, override val id: String) : ComponentI {

	val parentPath = parent

	override fun init() {

	}

	override fun getAllFields(): Set<ComponentField<*>> {
		return getAllNewFieldsExt()
	}

	override fun setValue(key: String, value: String) {
		val field = getAllNewFieldsExt().firstOrNull { it.id == key } ?: return
		field.set(value)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <F : ComponentField<*>> getField(id: String): F? {
		val f = getAllNewFieldsExt().firstOrNull { it.id == id } ?: return null
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
		for (f in getAllNewFieldsExt()) {
			copyFieldTo(dst.getAllNewFieldsExt(), f)
		}
	}

	override fun <T> copyFieldTo(dst: Collection<ComponentField<*>>, field: ComponentField<T>) {
		val dstField = getMatchingField(dst, field)
		if (dstField == null) {
			GameEngineI.warn("Copying component $this, $dst did not have field ${field.id}")
			return
		}
		field.copyTo(dstField)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getMatchingField(other: Collection<ComponentField<*>>, field: ComponentField<T>): ComponentField<T>? {
		return try {
			other.firstOrNull { it.id == field.id } as? ComponentField<T>
		} catch (e: Exception) {
			null
		}
	}

	override fun delete() {

	}

	override fun toString(): String {
		return "Component[$id]"
	}

	override fun serialise(head: StringBuilder, data: StringBuilder) {
		val nameStr = this::class.simpleName ?: "Anon"
		val properties = getAllNewFieldsExt()
		head.append(nameStr.length.toByteString(1) + nameStr + properties.size.toByteString(1))
		properties.forEach { it.serialise(head, data) }
	}
}

fun <C : Component> C.applied(): C {
	parent.components.add(this)
	return this
}

fun <C: Any> C.getAllNewFieldsExt(parent: String = ""): Set<ComponentField<*>> {
	val properties = this::class.memberProperties.filterIsInstance<KMutableProperty1<C, Any>>()
	val fields = mutableSetOf<ComponentField<*>>()
	for(p in properties){
		if(p.visibility != KVisibility.PUBLIC || p.setter.visibility != KVisibility.PUBLIC || p.hasAnnotation<EditorIgnore>()) continue
		val field = Components.getNewDefaultField(p, this, parent)
		if(field != null) fields.add(field)
		else {
			val value = p.get(this)
			fields.addAll(value.getAllNewFieldsExt(parent + p.name + '.'))
		}
	}
	return fields
}
