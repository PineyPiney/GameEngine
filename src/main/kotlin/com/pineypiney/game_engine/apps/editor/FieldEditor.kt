package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.fields.*
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.slider.ActionIntSliderComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.CheckBox
import com.pineypiney.game_engine.objects.menu_items.slider.BasicActionSlider
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.capitalise
import com.pineypiney.game_engine.util.s
import com.pineypiney.game_engine.window.Viewport
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import glm_.vec4.swizzle.xyz
import java.io.File
import kotlin.reflect.KClass

abstract class FieldEditor<T, out F : ComponentField<T>>(
	parent: GameObject,
	val field: F,
	position: Vec2i,
	size: Vec2i
) : DefaultInteractorComponent(parent), PostChildrenInit {

	init {
		parent.components.add(PixelTransformComponent(parent, position - Vec2i(0, size.y), size, Vec2(0f, 1f)))
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

	fun createText(text: String = field.id.substringAfterLast('.').capitalise(), alignment: Int = Text.ALIGN_CENTER_RIGHT, pos: Vec2 = Vec2(0f), size: Vec2 = Vec2(.25f, 1f)) = Text.makeMenuText(text, Vec4(0f, 0f, 0f, 1f), 16, alignment).apply { position = Vec3(pos, .01f); scale = Vec3(size, 1f) }

	companion object {
		init {
			addEditor(BoolField::class, ::BoolFieldEditor)
		}
	}
}

open class DefaultFieldEditor<T, F : ComponentField<T>>(parent: GameObject, field: F, position: Vec2i,
														size: Vec2i, callback: (String, String, String) -> Unit) : FieldEditor<T, F>(parent, field, position, size) {

	val nameText = createText()

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Text Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f)) { f, _, _ ->
		try {
			val oldSer = field.serialise(field.getter())
			field.parse(f.text)?.let { field.setter(it) }
			callback(field.id, oldSer, f.text)
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
	}

	override fun update() {
		textField.text = field.serialise(field.getter())
	}
}

open class BoolFieldEditor(parent: GameObject, field: BoolField, position: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) : FieldEditor<Boolean, BoolField>(parent, field, position, size), UpdatingAspectRatioComponent{

	val nameText = createText()

	val checkBox = CheckBox("Bool Field '${field.id}' Checkbox", Vec2i(0), Vec2i(16), Vec2(.27, 0f), field.getter()){
		field.setter(it)
		callback(field.id, field.serialise(!it), field.serialise(it))
	}

	override fun createChildren() {
		parent.addChild(nameText, checkBox)
		asp()
	}

	override fun update() {
		checkBox.boxComp.ticked = field.getter()
	}

	override fun updateAspectRatio(view: Viewport) {
		asp()
	}

	fun asp(){
		checkBox.scale = Vec3(parent.transformComponent.worldScale.run { y / x }, 1f, 1f)
	}
}

open class PrimitiveFieldEditor<T: Any, F: ComponentField<T>>(parent: GameObject, field: F,
															  position: Vec2i, size: Vec2i, parse: (String) -> T, callback: (String, String, String) -> Unit
) : FieldEditor<T, F>(parent, field, position, size){

	val nameText = createText()

	val textField = ActionTextField<TextFieldComponent>("Number Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f), field.getter().toString(), 16) { f, _, _ ->
		try {
			val value = parse(f.text)
			val oldSer = field.serialise(field.getter())
			field.setter(value)
			callback(field.id, oldSer, field.serialise(value))
		} catch (_: NumberFormatException) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
	}

	override fun update() {
		textField.text = field.getter().toString()
	}
}

open class IntFieldEditor(parent: GameObject, field: IntField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit): 
	PrimitiveFieldEditor<Int, IntField>(parent, field, origin, size, Integer::parseInt, callback)

open class UIntFieldEditor(parent: GameObject, field: UIntField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit):
	PrimitiveFieldEditor<UInt, UIntField>(parent, field, origin, size, { it.toUIntOrNull() ?: 0u }, callback)

open class FloatFieldEditor(parent: GameObject, field: FloatField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit):
	PrimitiveFieldEditor<Float, FloatField>(parent, field, origin, size, java.lang.Float::parseFloat, callback)

open class DoubleFieldEditor(parent: GameObject, field: DoubleField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit):
	PrimitiveFieldEditor<Double, DoubleField>(parent, field, origin, size, java.lang.Double::parseDouble, callback)

open class IntRangeFieldEditor(parent: GameObject, field: IntRangeField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit)
	: FieldEditor<Int, IntRangeField>(parent, field, origin, size){

	val nameText = createText()

	val slider = BasicActionSlider("Int Slider", Vec2(.27f, 0f), Vec2(.7f, 1f), field.range.first, field.range.last, field.getter()){
		val oldSer = field.serialise(field.getter())
		field.setter(it.value)
		callback(field.id, oldSer, field.serialise(it.value))

	}

	override fun createChildren() {
		parent.addChild(nameText, slider)
	}

	override fun update() {
		slider.getComponent<ActionIntSliderComponent>()?.value = field.getter()
	}

}

open class ShaderFieldEditor(parent: GameObject, field: ShaderField, origin: Vec2i, size: Vec2i, val callback: (String, String, String) -> Unit)
	: FieldEditor<Shader, ShaderField>(parent, field, origin, Vec2i(size.x, size.y * 3)) {

	val vertexText = createText("Vertex", pos = Vec2(0f, .68f), size = Vec2(.25f, .32f))
	val vertexField = ActionTextField<ActionTextFieldComponent<*>>("Vertex Field", Vec3(.27f, .68f, 0f), Vec2(.7f, .32f), field.getter().vName, 16) { _, _, _ ->
		updateValue()
	}
	val fragmentText = createText("Fragment", pos = Vec2(0f, .34f), size = Vec2(.25f, .32f))
	val fragmentField = ActionTextField<ActionTextFieldComponent<*>>("Fragment Field", Vec3(.27f, .34f, 0f), Vec2(.7f, .32f), field.getter().fName, 16) { _, _, _ ->
		updateValue()
	}
	val geometryText = createText("Geometry", pos = Vec2(0f, .0f), size = Vec2(.25f, .32f))
	val geometryField = ActionTextField<ActionTextFieldComponent<*>>("Geometry Field", Vec3(.27f, 0f, 0f), Vec2(.7f, .32f), field.getter().gName ?: "", 16) { _, _, _ ->
		updateValue()
	}

	fun updateValue(){
		try {
			val newS = if(geometryField.text.isEmpty()) ShaderLoader[ResourceKey(vertexField.text), ResourceKey(fragmentField.text)]
			else ShaderLoader[ResourceKey(vertexField.text), ResourceKey(fragmentField.text), ResourceKey(geometryField.text)]
			val oldSer = field.serialise(field.getter())
			field.setter(newS)
			callback(field.id, oldSer, field.serialise(newS))
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(vertexText, vertexField, fragmentText, fragmentField, geometryText, geometryField)
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
				val renderer: ColourRendererComponent = f.getComponent<ColourRendererComponent>() ?: continue
				renderer.colour.xyz = if (m == i) Vec3(0.65f) else Vec3(.5f)
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

open class TextureFieldEditor(parent: GameObject, field: TextureField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit)
	: FieldEditor<Texture, TextureField>(parent, field, origin, size) {

	val nameText = createText()

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Texture Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f), field.serialise(field.getter()), 16) { f, _, _ ->
		try {
			val oldSer = field.serialise(field.getter())
			field.parse(f.text)?.let { field.setter(it) }
			callback(field.id, oldSer, f.text)
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
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

open class ModelFieldEditor(parent: GameObject, field: ModelField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit)
	: FieldEditor<Model, ModelField>(parent, field, origin, size) {

	val nameText = createText()

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Model Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f), field.serialise(field.getter()), 16) { f, _, _ ->
		try {
			val oldSer = field.serialise(field.getter())
			field.parse(f.text)?.let { field.setter(it) }
			callback(field.id, oldSer, f.text)
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
	}

	override fun update() {
		textField.text = field.serialise(field.getter())
	}

	override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
		val renderer = textField.getComponent<ColourRendererComponent>()
		val willAccept = element is File && arrayOf("glb", "gltf", "pgm").contains(element.extension)
		renderer?.colour?.xyz = if(willAccept) Vec3(0.65f) else Vec3(.5f)
		return willAccept
	}

	override fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen) {
		if(element is File){
			val path = element.path.replace(s, '/').removePrefix(screen.gameEngine.resourcesLoader.location + '/' + screen.gameEngine.resourcesLoader.modelLocation).substringBefore('.')
			field.setter(ModelLoader[ResourceKey(path)])
			textField.text = path
		}
	}
}

/*
class CollectionFieldEditor<T, C : Collection<T>>(
	parent: GameObject,
	field: com.pineypiney.game_engine.objects.fields.fields.CollectionField<T, C>,
	
	origin: Vec2,
	size: Vec2,
	val editor: FieldCreator<T>,
	callback: (String, String, String) -> Unit
) : FieldEditor<C, CollectionField<T, C>>(parent, field, origin, size) {

	val coll = field.getter()
	val textFields = this@CollectionFieldEditor.field.getter().mapIndexed { i, _ ->
		val entryID = "$id#$i"
		Components.getNewDefaultField()
		editor(MenuItem("Field Editor $entryID"), field, entryID, origin, size, callback).applied()
	}

	override fun createChildren() {
		for(t in textFields) parent.addChild(t.parent)
	}

	override fun update() {
		val v = this@CollectionFieldEditor.field.getter()
		v.forEachIndexed { index, t ->
			textFields[index].update()
		}
	}
}

 */

open class VecTFieldEditor<T: Number, V, out F : ComponentField<V>>(
	parent: GameObject,
	field: F,
	origin: Vec2i,
	size: Vec2i,
	callback: (String, String, String) -> Unit,
	vecSize: Int,
	parseT: (String) -> T,
	copy: (V) -> V,
	val inGet: V.(Int) -> T,
	inSet: V.(Int, T) -> Unit
) : FieldEditor<V, F>(parent, field, origin, size) {

	val nameField = createText()

	val x = (.7f - ((vecSize - 1) * .01f)) / vecSize
	val textFields = Array<ActionTextField<*>>(vecSize) {
		ActionTextField<ActionTextFieldComponent<*>>(
			"Vec Field $it",
			Vec3(.27f + it * (x + .01f), 0f, 0f),
			Vec2(x, 1f),
			field.getter().inGet(it).toString(),
			16
		) { f, _, _ ->
			try {
				val oldVal = field.getter()
				val oldSer = field.serialise(oldVal)
				val newVal = copy(oldVal)
				newVal.inSet(it, parseT(f.text))
				field.setter(newVal)
				callback(field.id, oldSer, field.serialise(newVal))
				f.text = field.getter().inGet(it).toString()
			} catch (_: NumberFormatException) {

			}
		}.also { f -> f.name = "${parent.name.substring(0, parent.name.length - 13)}.${names[it]} Field Editor" }
	}

	override fun createChildren() {
		parent.addChild(nameField, *textFields)
	}

	override fun update() {
		val v = try {
			field.getter()
		}
		catch (_: NullPointerException){
			return
		}
		textFields.forEachIndexed { index, actionTextField ->
			actionTextField.text = v.inGet(index).toString()
		}
	}

	companion object {
		val names = charArrayOf('x', 'y', 'z', 'w')
	}
}

open class VeciFieldEditor<V, out F : ComponentField<V>>(parent: GameObject, field: F, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit, vecSize: Int, copy: (V) -> V, inGet: V.(Int) -> Int, inSet: V.(Int, Int) -> Unit): 
	VecTFieldEditor<Int, V, F>(parent, field, origin, size, callback, vecSize, Integer::parseInt, copy, inGet, inSet)

open class Vec2iFieldEditor(parent: GameObject, field: Vec2iField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) :
	VeciFieldEditor<Vec2i, Vec2iField>(parent, field, origin, size, callback, 2, ::Vec2i, Vec2i::get, Vec2i::set)

open class Vec3iFieldEditor(parent: GameObject, field: Vec3iField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) :
	VeciFieldEditor<Vec3i, Vec3iField>(parent, field, origin, size, callback, 3, ::Vec3i, Vec3i::get, Vec3i::set)

open class Vec4iFieldEditor(parent: GameObject, field: Vec4iField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) :
	VeciFieldEditor<Vec4i, Vec4iField>(parent, field, origin, size, callback, 4, ::Vec4i, Vec4i::get, Vec4i::set)


open class VecFieldEditor<V, out F : ComponentField<V>>(parent: GameObject, field: F, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit, vecSize: Int, copy: (V) -> V, inGet: V.(Int) -> Float, inSet: V.(Int, Float) -> Unit) : 
	VecTFieldEditor<Float, V, F>(parent, field, origin, size, callback, vecSize, java.lang.Float::parseFloat, copy, inGet, inSet)

open class Vec2FieldEditor(parent: GameObject, field: Vec2Field, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) : 
	VecFieldEditor<Vec2, Vec2Field>(parent, field, origin, size, callback, 2, ::Vec2, Vec2::get, Vec2::set)

open class Vec3FieldEditor(parent: GameObject, field: Vec3Field, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) : 
	VecFieldEditor<Vec3, Vec3Field>(parent, field, origin, size, callback, 3, ::Vec3, Vec3::get, Vec3::set)

open class Vec4FieldEditor(parent: GameObject, field: Vec4Field, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) : 
	VecFieldEditor<Vec4, Vec4Field>(parent, field, origin, size, callback, 4, ::Vec4, Vec4::get, Vec4::set)

open class QuatFieldEditor(parent: GameObject, field: QuatField, origin: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) : 
	VecFieldEditor<Quat, QuatField>(parent, field, origin, size, callback, 4, ::Quat, Quat::get, Quat::set)


class EditorType<T, F: ComponentField<T>>(val klass: KClass<F>, val creator: EditorCreator<F>)

val editors = mutableListOf<EditorType<*, *>>(
	EditorType(BoolField::class, ::BoolFieldEditor),
	EditorType(IntField::class, ::IntFieldEditor),
	EditorType(IntRangeField::class, ::IntRangeFieldEditor),
	EditorType(UIntField::class, ::UIntFieldEditor),
	EditorType(FloatField::class, ::FloatFieldEditor),
	EditorType(DoubleField::class, ::DoubleFieldEditor),
	EditorType(Vec2iField::class, ::Vec2iFieldEditor),
	EditorType(Vec3iField::class, ::Vec3iFieldEditor),
	EditorType(Vec4iField::class, ::Vec4iFieldEditor),
	EditorType(Vec2Field::class, ::Vec2FieldEditor),
	EditorType(Vec3Field::class, ::Vec3FieldEditor),
	EditorType(Vec4Field::class, ::Vec4FieldEditor),
	EditorType(QuatField::class, ::QuatFieldEditor),
	EditorType(ShaderField::class, ::ShaderFieldEditor),
	EditorType(TextureField::class, ::TextureFieldEditor),
	EditorType(ModelField::class, ::ModelFieldEditor),
)

fun <T, F: ComponentField<T>> addEditor(klass: KClass<F>, creator: EditorCreator<F>){
	editors.add(EditorType(klass, creator))
}

@Suppress("UNCHECKED_CAST")
fun <T, F: ComponentField<T>> createEditor(parent: GameObject, field: F, position: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit): FieldEditor<*, *> {
	val creator: EditorType<T, F> = (editors.firstOrNull { it.klass == field::class } as? EditorType<T, F>) ?: return DefaultFieldEditor(parent, field, position, size, callback)
	return creator.creator(parent, field, position, size, callback)
}

typealias EditorCreator<F> = (parent: GameObject, field: F, position: Vec2i, size: Vec2i, callback: (String, String, String) -> Unit) -> FieldEditor<*, F>

