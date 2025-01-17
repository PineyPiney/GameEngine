package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.field_editors.QuatFieldEditor
import com.pineypiney.game_engine.apps.editor.field_editors.Vec2FieldEditor
import com.pineypiney.game_engine.apps.editor.field_editors.Vec3FieldEditor
import com.pineypiney.game_engine.apps.editor.field_editors.Vec4FieldEditor
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component.Field
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.fromString
import com.pineypiney.game_engine.util.extension_functions.toByteString
import com.pineypiney.game_engine.util.extension_functions.toString
import com.pineypiney.game_engine.util.s
import glm_.asHexString
import glm_.i
import glm_.intValue
import glm_.plus
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.swizzle.xyz
import java.io.File
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

	@Suppress("UNCHECKED_CAST")
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

	@Suppress("UNCHECKED_CAST")
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
				s, false, true,
				ByteData::string2Float
			)
		}, { Quat(it.w, it.x, it.y, it.z) })

	class TextureField(id: String, getter: () -> Texture, setter: (Texture) -> Unit): Field<Texture>(id, ::TextureFieldEditor, getter, setter,
		{ it.fileLocation.substringBefore('.') },
		{ _, s -> TextureLoader[ResourceKey(s)] })

	class ShaderField(id: String, getter: () -> Shader, setter: (Shader) -> Unit): Field<Shader>(id, ::ShaderFieldEditor, getter, setter, ::serialise, ::parse){

		companion object {
			fun serialise(shader: Shader): String {
				val sb = StringBuilder()
				val g = shader.gName != null
				sb.append(if (g) '3' else '2')
				sb.append(shader.vName.length.toChar() + shader.vName)
				sb.append(shader.fName.length.toChar() + shader.fName)
				if (g) sb.append(shader.gName.length.toChar() + shader.gName)
				return sb.toString()
			}

			fun parse(c: ComponentI, s: String): Shader{
				val hasG = s[0] == '3'
				val vl = s[1].code
				val v = s.substring(2, 2+vl)
				val fl = s[2+vl].code
				val f = s.substring(3+vl, 3+vl+fl)
				if(hasG){
					val gl = s[3+vl+fl]
					val g = s.substring(4+vl+fl, 4+vl+fl+gl)
					return ShaderLoader[ResourceKey(v), ResourceKey(f), ResourceKey(g)]
				}
				else return ShaderLoader[ResourceKey(v), ResourceKey(f)]
			}
		}
	}

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

			@Suppress("UNCHECKED_CAST")
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
		{ obj, c, i, o, s, cb -> CollectionFieldEditor(obj, c, i, o, s, subEditor, cb) },
		getter,
		setter,
		{ it.joinToString(separator, transform = serialise) },
		{ c, s ->
			collectionConverter(s.split(separator).mapNotNull { parse(c, it) })
		})

	abstract class FieldEditor<T, out F : Field<T>>(
		parent: GameObject,
		component: ComponentI,
		val fullId: String,
		origin: Vec2,
		size: Vec2
	) : DefaultInteractorComponent(parent, "FDE"), PostChildrenInit {

		val fieldID = fullId.substringAfterLast('.')
		val field: F = component.getField(fieldID.substringBefore('#'))
			?: throw Exception("Component $component does not contain field $fieldID}")

		init {
			parent.position = Vec3(origin, 0f)
			parent.scale = Vec3(size, 1f)
			passThrough = true
		}

		override fun init() {
			super.init()
			createChildren()
		}

		override fun postChildrenInit() {
			update()
		}

		abstract fun createChildren()

		abstract fun update()

		open fun onHoverElement(element: Any, cursorPos: Vec2): Boolean = false

		open fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen){}
	}

	open class DefaultFieldEditor<T, F : Field<T>>(
		parent: GameObject,
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<T, F>(parent, component, id, origin, size) {

		val textField = ActionTextField<ActionTextFieldComponent<*>>("Text Field", Vec3(0f, 0f, .01f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				field.parse(component, f.text)?.let { field.setter(it) }
				callback(fullId, f.text)
			} catch (_: Exception) {

			}
		}

		override fun createChildren() {
			parent.addChild(textField)
		}

		override fun update() {
			textField.text = field.serialise(field.getter())
		}
	}

	open class FloatFieldEditor(
		parent: GameObject,
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<Float, FloatField>(parent, component, id, origin, size) {

		val textField = ActionTextField<TextFieldComponent>("Floaat Field", Vec3(0f, 0f, .01f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				val value = java.lang.Float.parseFloat(f.text)
				field.setter(value)
				callback(fullId, field.serialise(value))
			} catch (_: NumberFormatException) {

			}
		}

		override fun createChildren() {
			parent.addChild(textField)
		}

		override fun update() {
			textField.text = field.getter().toString()
		}
	}

	open class DoubleFieldEditor(
		parent: GameObject,
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<Double, DoubleField>(parent, component, id, origin, size) {

		val textField = ActionTextField<TextFieldComponent>("Double Field", Vec3(0f, 0f, .01f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				val value = java.lang.Double.parseDouble(f.text)
				field.setter(value)
				callback(fullId, field.serialise(value))
			} catch (_: NumberFormatException) {

			}
		}

		override fun createChildren() {
			parent.addChild(textField)
		}

		override fun update() {
			textField.text = field.getter().toString()
		}
	}

	open class TextureFieldEditor(
		parent: GameObject,
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		callback: (String, String) -> Unit
	) : FieldEditor<Texture, TextureField>(parent, component, id, origin, size) {

		val textField = ActionTextField<ActionTextFieldComponent<*>>("Texture Field", Vec3(0f, 0f, -0.005f), Vec2(1f, 1f)) { f, _, _ ->
			try {
				field.parse(component, f.text)?.let { field.setter(it) }
				callback(fullId, f.text)
			} catch (_: Exception) {

			}
		}

		override fun createChildren() {
			parent.addChild(textField)
		}

		override fun update() {
			textField.text = field.serialise(field.getter())
		}

		override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
			val renderer = textField.getComponent<ColourRendererComponent>()
			val willAccept = element is File && element.extension == "png"
			renderer?.colour?.xyz = if(willAccept) Vec3(0.65f) else Vec3(.5f)
			return willAccept
		}

		override fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen) {
			if(element is File){
				val path = element.path.replace(s, '/').removePrefix(screen.gameEngine.resourcesLoader.location + '/' + screen.gameEngine.resourcesLoader.textureLocation).substringBefore('.')
				field.setter(TextureLoader[ResourceKey(path)])
				textField.text = path
			}
		}
	}

	open class ShaderFieldEditor(
		parent: GameObject,
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		val callback: (String, String) -> Unit
	) : FieldEditor<Shader, ShaderField>(parent, component, id, origin, Vec2(size.x, size.y * 3)) {

		val vertexField = ActionTextField<ActionTextFieldComponent<*>>("Vertex Field", Vec3(0f, .68f, -0.005f), Vec2(1f, .32f)) { f, _, _ ->
			updateValue()
		}
		val fragmentField = ActionTextField<ActionTextFieldComponent<*>>("Fragment Field", Vec3(0f, .34f, -0.005f), Vec2(1f, .32f)) { f, _, _ ->
			updateValue()
		}
		val geometryField = ActionTextField<ActionTextFieldComponent<*>>("Geometry Field", Vec3(0f, 0f, -0.005f), Vec2(1f, .32f)) { f, _, _ ->
			updateValue()
		}

		fun updateValue(){
			try {
				val newS = if(geometryField.text.isEmpty()) ShaderLoader[ResourceKey(vertexField.text), ResourceKey(fragmentField.text)]
				else ShaderLoader[ResourceKey(vertexField.text), ResourceKey(fragmentField.text), ResourceKey(geometryField.text)]
				field.setter(newS)
				callback(fullId, ShaderField.serialise(newS))
			} catch (_: Exception) {

			}
		}

		override fun createChildren() {
			parent.addChild(vertexField, fragmentField, geometryField)
		}

		override fun update() {
			val s = field.getter()
			vertexField.text = s.vName
			fragmentField.text = s.fName
			geometryField.text = s.gName ?: ""
		}

		override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
			if(element is File){
				val fields = arrayOf(vertexField, fragmentField, geometryField)
				val relCur = (cursorPos.y - parent.transformComponent.worldPosition.y) / parent.transformComponent.worldScale.y
				val m = if(element.extension == "vs" && relCur >= .67f) 0
				else if(element.extension == "fs" && relCur >= .33f && relCur <= .67f) 1
				else if(element.extension == "gs" && relCur <= .33f) 2
				else -1

				for((i, f) in fields.withIndex()) {
					val renderer: ColourRendererComponent? = f.getComponent<ColourRendererComponent>() ?: continue
					renderer?.colour?.xyz = if (m == i) Vec3(0.65f) else Vec3(.5f)
				}
				return m != -1
			}
			return false
		}

		override fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen) {
			if(element is File){
				val path = element.path.replace(s, '/').removePrefix(screen.gameEngine.resourcesLoader.location + '/' + screen.gameEngine.resourcesLoader.shaderLocation).substringBefore('.')
				when(element.extension){
					"vs" -> vertexField.text = path
					"fs" -> fragmentField.text = path
					"gs" -> geometryField.text = path
					else -> return
				}
				updateValue()
			}
		}
	}

	class CollectionFieldEditor<T, C : Collection<T>>(
		parent: GameObject,
		component: ComponentI,
		id: String,
		origin: Vec2,
		size: Vec2,
		val editor: FieldCreator<T>,
		callback: (String, String) -> Unit
	) : FieldEditor<C, CollectionField<T, C>>(parent, component, id, origin, size) {

		val textFields = field.getter().mapIndexed { i, _ ->
			val entryID = "$id#$i"
			editor(MenuItem("Field Editor $entryID"), component, entryID, origin, size, callback).applied()
		}

		override fun createChildren() {
			for(t in textFields) parent.addChild(t.parent)
		}

		override fun update() {
			val v = field.getter()
			v.forEachIndexed { index, t ->
				textFields[index].update()
			}
		}
	}
}

typealias FieldCreator<T> = (parent: GameObject, component: ComponentI, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) -> Component.FieldEditor<T, Field<T>>

fun <C : Component> C.applied(): C {
	parent.components.add(this)
	return this
}

fun <C: ComponentI> C.getAllFieldsExt(): Array<Field<*>> {
	val properties = this::class.memberProperties.filterIsInstance<KMutableProperty1<C, Any>>().filter{ it.annotations.any { a -> a is EditingField } }
	val fs = properties.mapNotNull { Components.getDefaultField(it, this) }
	return fs.toTypedArray()
}
