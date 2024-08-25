package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component.Field
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromString
import com.pineypiney.game_engine.util.extension_functions.toByteString
import com.pineypiney.game_engine.util.extension_functions.toString
import glm_.*
import glm_.quat.Quat
import glm_.vec1.Vec1Vars
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

abstract class Component(final override val parent: GameObject, override val id: String) : ComponentI {

	val parentPath = parent

	override val fields: Array<Field<*>>
		get() = getAllFields()

	override fun init() {

	}

	override fun getAllFields(): Array<Field<*>>{
		return getAllFieldsExt()
	}

	override fun setValue(key: String, value: String) {
		val field = fields.firstOrNull { it.id == key } ?: return
		field.set(this, value)
	}

	override fun <F : Field<*>> getField(id: String): F? {
		val f = fields.firstOrNull { it.id == id } ?: return null
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
		for (f in fields) {
			copyFieldTo(dst, f)
		}
	}

	override fun <T> copyFieldTo(dst: ComponentI, field: Field<T>) {
		val dstField = getMatchingField(dst, field)
		if (dstField == null) {
			GameEngineI.warn("Copying component $this, $dst did not have field ${field.id}")
			return
		}
		field.copyTo(dstField)
	}

	override fun <T> getMatchingField(other: ComponentI, field: Field<T>): Field<T>? {
		return try {
			other.fields.firstOrNull { it.id == field.id } as? Field<T>
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
		head.append(nameStr.length.toByteString(1) + nameStr + fields.size.toByteString(1))
		fields.forEach { it.serialise(head, data) }
	}

	open class Field<T>(
		val id: String,
		val editor: FieldCreator<T>,
		val getter: () -> T,
		val setter: (T) -> Unit,
		val serialise: (T) -> String,
		val parse: (ComponentI, String) -> T?,
		val copy: (T) -> T = { it }
	) {
		fun set(component: ComponentI, value: String) {
			setter(parse(component, value) ?: return)
		}

		fun copyTo(other: Field<T>) {
			other.setter(copy(getter()))
		}

		override fun toString(): String {
			return "ComponentField[$id]"
		}

		fun serialise(head: StringBuilder, data: StringBuilder) {
			val s = serialise(getter())
			head.append(id + s.length.toByteString())
			data.append(s)
		}
	}

	class IntField(id: String, getter: () -> Int, setter: (Int) -> Unit) : Field<Int>(id,
		::DefaultFieldEditor, getter, setter, { i -> i.asHexString }, { _, s -> s.intValue(16) })

	class UIntField(id: String, getter: () -> UInt, setter: (UInt) -> Unit) : Field<UInt>(id,
		::DefaultFieldEditor, getter, setter, { i -> i.toInt().asHexString }, { _, s -> s.intValue(16).toUInt() })

	class FloatField(id: String, getter: () -> Float, setter: (Float) -> Unit) : Field<Float>(id,
		::FloatFieldEditor, getter, setter,
		ByteData::float2String, { _, s -> ByteData.string2Float(s) }) {
	}

	class DoubleField(id: String, getter: () -> Double, setter: (Double) -> Unit) : Field<Double>(id,
		::DoubleFieldEditor, getter, setter,
		ByteData::double2String, { _, s -> ByteData.string2Double(s) }) {

	}

	class BooleanField(id: String, getter: () -> Boolean, setter: (Boolean) -> Unit) : Field<Boolean>(id,
		::DefaultFieldEditor, getter, setter, { b -> b.i.toString() }, { _, s -> s.toBoolean() })

	class Vec2Field(id: String, getter: () -> Vec2, setter: (Vec2) -> Unit) : Field<Vec2>(id,
		::Vec2FieldEditor, getter, setter, { v -> v.toString(",", ByteData::float2String) }, { _, s ->
			try {
				Vec2.fromString(s, false, ByteData::string2Float)
			} catch (e: NumberFormatException) {
				Vec2()
			}
		}, { Vec2(it.x, it.y) })

	class Vec3Field(id: String, getter: () -> Vec3, setter: (Vec3) -> Unit) : Field<Vec3>(id,
		::Vec3FieldEditor, getter, setter, { v -> v.toString(",", ByteData::float2String) }, { _, s ->
			Vec3.fromString(
				s, false,
				ByteData::string2Float
			)
		}, { Vec3(it.x, it.y, it.z) })

	class Vec4Field(id: String, getter: () -> Vec4, setter: (Vec4) -> Unit) : Field<Vec4>(id,
		::Vec4FieldEditor, getter, setter, { v -> v.toString(",", ByteData::float2String) }, { _, s ->
			Vec4.fromString(
				s, false,
				ByteData::string2Float
			)
		}, { Vec4(it.x, it.y, it.z, it.w) })

	class QuatField(id: String, getter: () -> Quat, setter: (Quat) -> Unit) : Field<Quat>(id,
		::QuatFieldEditor, getter, setter, { q -> q.toString(",", ByteData::float2String) }, { _, s ->
			Quat.fromString(
				s, false,
				ByteData::string2Float
			)
		}, { Quat(it.w, it.x, it.y, it.z) })

	class TextureField(id: String, getter: () -> Texture, setter: (Texture) -> Unit): Field<Texture>(id, ::TextureFieldEditor, getter, setter,
		{ it.fileLocation.substringBefore('.') },
		{ _, s -> TextureLoader[ResourceKey(s)] })

	class GameObjectField<T : GameObject?>(id: String, getter: () -> T, setter: (T) -> Unit) : Field<T>(
		id,
		::DefaultFieldEditor, getter, setter,
		Companion::serialise,
		Companion::parse
	) {

		companion object {
			fun <T : GameObject?> serialise(o: T): String {
				return o?.name ?: "Storable"
			}

			fun <T : GameObject?> parse(c: ComponentI, s: String): T? {
				return c.parent.objects?.getAllObjects()?.firstOrNull { it.name == s } as? T
			}
		}
	}

	class CollectionField<T, C : Collection<T>>(
		id: String,
		getter: () -> C,
		setter: (C) -> Unit,
		val separator: String,
		serialise: (T) -> String,
		parse: (ComponentI, String) -> T?,
		collectionConverter: (List<T>) -> C,
		val subEditor: FieldCreator<T>
	) : Field<C>(
		id,
		{ c, i, o, s, cb -> CollectionFieldEditor(c, i, o, s, subEditor, cb) },
		getter,
		setter,
		{ it.joinToString(separator, transform = serialise) },
		{ c, s ->
			collectionConverter(s.split(separator).mapNotNull { parse(c, it) })
		})

	abstract class FieldEditor<T, out F : Field<T>>(
		component: ComponentI,
		val fullId: String,
		origin: Vec2,
		size: Vec2
	) : MenuItem("$fullId Field Editor") {

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

	open class DefaultFieldEditor<T, F : Field<T>>(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<T, F>(component, id, origin, size) {

		val textField = ActionTextField<ActionTextFieldComponent<*>>("Text Field", Vec2(0f, 0f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				field.parse(component, f.text)?.let { field.setter(it) }
				callback(fullId, f.text)
			} catch (_: Exception) {

			}
		}

		override fun addChildren() {
			addChild(textField)
		}

		override fun update() {
			textField.text = field.serialise(field.getter())
		}
	}

	open class FloatFieldEditor(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<Float, FloatField>(component, id, origin, size) {

		val textField = ActionTextField<TextFieldComponent>("Floaat Field", Vec2(0f, 0f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				val value = java.lang.Float.parseFloat(f.text)
				field.setter(value)
				callback(fullId, field.serialise(value))
			} catch (_: NumberFormatException) {

			}
		}

		override fun addChildren() {
			addChild(textField)
		}

		override fun update() {
			textField.text = field.getter().toString()
		}
	}

	open class DoubleFieldEditor(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<Double, DoubleField>(component, id, origin, size) {

		val textField = ActionTextField<TextFieldComponent>("Double Field", Vec2(0f, 0f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				val value = java.lang.Double.parseDouble(f.text)
				field.setter(value)
				callback(fullId, field.serialise(value))
			} catch (_: NumberFormatException) {

			}
		}

		override fun addChildren() {
			addChild(textField)
		}

		override fun update() {
			textField.text = field.getter().toString()
		}
	}

	open class VecFieldEditor<T : Vec1Vars<Float>, out F : Field<T>>(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit,
		vecSize: Int,
		copy: (T) -> T,
		inSet: T.(Int, Float) -> Unit
	) : FieldEditor<T, F>(component, id, origin, size) {

		val fields = Array<ActionTextField<*>>(vecSize) {
			ActionTextField<ActionTextFieldComponent<*>>(
				"Vec Field $it",
				Vec2((size.x * it / vecSize), 0f),
				Vec2(size.x / vecSize, 1f)
			) { f, _, _ ->
				try {
					val newVal = copy(field.getter())
					newVal.inSet(it, java.lang.Float.parseFloat(f.text))
					field.setter(newVal)
					callback(fullId, field.serialise(newVal))
				} catch (_: NumberFormatException) {

				}
			}.also { f -> f.name = "${name.substring(0, name.length - 13)}.${names[it]} Field Editor" }
		}

		override fun addChildren() {
			addChild(*fields)
		}

		override fun update() {
			val v = field.getter()
			fields.forEachIndexed { index, actionTextField ->
				actionTextField.text = v[index].toString()
			}
		}

		companion object {
			val names = charArrayOf('x', 'y', 'z', 'w')
		}
	}

	open class Vec2FieldEditor(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : VecFieldEditor<Vec2, Vec2Field>(component, id, origin, size, callback, 2, ::Vec2, Vec2::set)

	open class Vec3FieldEditor(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : VecFieldEditor<Vec3, Vec3Field>(component, id, origin, size, callback, 3, ::Vec3, Vec3::set)

	open class Vec4FieldEditor(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : VecFieldEditor<Vec4, Vec4Field>(component, id, origin, size, callback, 4, ::Vec4, Vec4::set)


	open class QuatFieldEditor(component: ComponentI, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) : FieldEditor<Quat, QuatField>(component, id, origin, size) {

		val xField = ActionTextField<ActionTextFieldComponent<*>>("Quat X Field", Vec2(0f, 0f), Vec2(size.x * 0.25f, 1f)) { f, _, _ ->
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
			Vec2(size.x * 0.25f, 0f),
			Vec2(size.x * 0.25f, 1f)
		) { f, _, _ ->
			try {
				val v = field.getter()
				val newVal = Quat(v.w, v.x, java.lang.Float.parseFloat(f.text), v.z)
				field.setter(newVal)
				callback(fullId, field.serialise(newVal))
			} catch (_: NumberFormatException) {

			}
		}

		val zField =
			ActionTextField<ActionTextFieldComponent<*>>("Quat Z Field", Vec2(size.x * 0.5f, 0f), Vec2(size.x * 0.25f, 1f)) { f, _, _ ->
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

		val fields = arrayOf(xField, yField, zField, wField)

		override fun addChildren() {
			addChild(*fields)
		}

		override fun update() {
			val v = field.getter()
			xField.text = v.x.toString()
			yField.text = v.y.toString()
			zField.text = v.z.toString()
			wField.text = v.w.toString()
		}
	}

	open class TextureFieldEditor(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<Texture, TextureField>(component, id, origin, size) {

		val textField = ActionTextField<ActionTextFieldComponent<*>>("Texture Field", Vec2(0f, 0f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				field.parse(component, f.text)?.let { field.setter(it) }
				callback(fullId, f.text)
			} catch (_: Exception) {

			}
		}

		override fun addChildren() {
			addChild(textField)
		}

		override fun update() {
			textField.text = field.serialise(field.getter())
		}
	}

	class CollectionFieldEditor<T, C : Collection<T>>(
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		val editor: FieldCreator<T>,
		callback: (String, String) -> Unit
	) : FieldEditor<C, CollectionField<T, C>>(component, id, origin, size) {

		val fields = field.getter().mapIndexed { i, _ ->
			editor(component, "$id#$i", origin, size, callback)
		}

		override fun addChildren() {
			addChildren(fields)
		}

		override fun update() {
			val v = field.getter()
			v.forEachIndexed { index, t ->
				fields[index].update()
			}
		}
	}
}

typealias FieldCreator<T> = (component: ComponentI, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) -> Component.FieldEditor<T, Field<T>>

fun <C : Component> C.applied(): C {
	parent.components.add(this)
	return this
}

fun <C: ComponentI> C.getAllFieldsExt(): Array<Field<*>> {
	val properties = this::class.memberProperties.filterIsInstance<KMutableProperty1<C, Any>>().filter{ it.annotations.any { a -> a is EditingField } }
	val fs = properties.mapNotNull { Components.getDefaultField(it, this) }
	return fs.toTypedArray()
}
