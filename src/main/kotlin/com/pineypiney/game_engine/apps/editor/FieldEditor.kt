package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.fields.*
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.slider.ActionIntSliderComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.CheckBox
import com.pineypiney.game_engine.objects.menu_items.slider.BasicActionSlider
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.s
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.swizzle.xyz
import java.io.File

abstract class FieldEditor<T, out F : ComponentField<T>>(
	parent: GameObject,
	val field: F,
	origin: Vec2,
	size: Vec2
) : DefaultInteractorComponent(parent, "FDE"), PostChildrenInit {

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

open class DefaultFieldEditor<T, F : ComponentField<T>>(parent: GameObject, field: F, origin: Vec2,
	size: Vec2, callback: (String, String) -> Unit) : FieldEditor<T, F>(parent, field, origin, size) {

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Text Field", Vec3(0f, 0f, .01f), Vec2(1f, 1f)) { f, _, _ ->
		try {
			field.parse(f.text)?.let { field.setter(it) }
			callback(field.id, f.text)
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

open class BoolFieldEditor(parent: GameObject, field: BoolField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) : FieldEditor<Boolean, BoolField>(parent, field, origin, size), UpdatingAspectRatioComponent{

	val checkBox = CheckBox("Bool Checkbox", field.getter()){
		field.setter(it)
		callback(field.id, field.serialise(it))
	}

	override fun createChildren() {
		parent.addChild(checkBox)
		asp()
	}

	override fun update() {
		checkBox.boxComp.ticked = field.getter()
	}

	override fun updateAspectRatio(renderer: RendererI) {
		asp()
	}

	fun asp(){
		checkBox.scale = Vec3(parent.transformComponent.worldScale.run { y / x }, 1f, 1f)
	}
}

open class PrimitiveFieldEditor<T: Any, F: ComponentField<T>>(parent: GameObject, field: F,
															  origin: Vec2, size: Vec2, parse: (String) -> T, callback: (String, String) -> Unit
) : FieldEditor<T, F>(parent, field, origin, size){

	val textField = ActionTextField<TextFieldComponent>("Number Field", Vec3(0f, 0f, .01f), Vec2(1f, 1f)) { f, _, _ ->
		try {
			val value = parse(f.text)
			field.setter(value)
			callback(field.id, field.serialise(value))
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

open class IntFieldEditor(parent: GameObject, field: IntField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): 
	PrimitiveFieldEditor<Int, IntField>(parent, field, origin, size, Integer::parseInt, callback)

open class UIntFieldEditor(parent: GameObject, field: UIntField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit):
	PrimitiveFieldEditor<UInt, UIntField>(parent, field, origin, size, { it.toUIntOrNull() ?: 0u }, callback)

open class FloatFieldEditor(parent: GameObject, field: FloatField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit):
	PrimitiveFieldEditor<Float, FloatField>(parent, field, origin, size, java.lang.Float::parseFloat, callback)

open class DoubleFieldEditor(parent: GameObject, field: DoubleField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit):
	PrimitiveFieldEditor<Double, DoubleField>(parent, field, origin, size, java.lang.Double::parseDouble, callback)

open class IntRangeFieldEditor(parent: GameObject, field: IntRangeField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit)
	: FieldEditor<Int, IntRangeField>(parent, field, origin, size){

	val slider = BasicActionSlider("Int Slider", field.range.first, field.range.last, field.getter()){
		field.setter(it.value)
		callback(field.id, field.serialise(it.value))

	}

	override fun createChildren() {
		parent.addChild(slider)
	}

	override fun update() {
		slider.getComponent<ActionIntSliderComponent>()?.value = field.getter()
	}

}

open class ShaderFieldEditor(parent: GameObject, field: ShaderField, origin: Vec2, size: Vec2, val callback: (String, String) -> Unit)
	: FieldEditor<Shader, ShaderField>(parent, field, origin, Vec2(size.x, size.y * 3)) {

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
			callback(field.id, ShaderField.serialise(newS))
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

open class TextureFieldEditor(parent: GameObject, field: TextureField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit)
	: FieldEditor<Texture, TextureField>(parent, field, origin, size) {

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Texture Field", Vec3(0f, 0f, -0.005f), Vec2(1f, 1f)) { f, _, _ ->
		try {
			field.parse(f.text)?.let { field.setter(it) }
			callback(field.id, f.text)
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

open class ModelFieldEditor(parent: GameObject, field: ModelField, origin: Vec2, size: Vec2, callback: (String, String) -> Unit)
	: FieldEditor<Model, ModelField>(parent, field, origin, size) {

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Model Field", Vec3(0f, 0f, -0.005f), Vec2(1f, 1f)) { f, _, _ ->
		try {
			field.parse(f.text)?.let { field.setter(it) }
			callback(field.id, f.text)
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
	callback: (String, String) -> Unit
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

open class VecFieldEditor<T, out F : ComponentField<T>>(
	parent: GameObject,
	field: F,
	
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit,
	vecSize: Int,
	copy: (T) -> T,
	val inGet: T.(Int) -> Float,
	inSet: T.(Int, Float) -> Unit
) : FieldEditor<T, F>(parent, field, origin, size) {

	val textFields = Array<ActionTextField<*>>(vecSize) {
		ActionTextField<ActionTextFieldComponent<*>>(
			"Vec Field $it",
			Vec3((size.x * it / vecSize), 0f, .01f),
			Vec2(size.x / vecSize, 1f)
		) { f, _, _ ->
			try {
				val newVal = copy(field.getter())
				newVal.inSet(it, java.lang.Float.parseFloat(f.text))
				field.setter(newVal)
				callback(field.id, field.serialise(newVal))
			} catch (_: NumberFormatException) {

			}
		}.also { f -> f.name = "${parent.name.substring(0, parent.name.length - 13)}.${names[it]} Field Editor" }
	}

	override fun createChildren() {
		parent.addChild(*textFields)
	}

	override fun update() {
		val v = field.getter()
		textFields.forEachIndexed { index, actionTextField ->
			actionTextField.text = v.inGet(index).toString()
		}
	}

	companion object {
		val names = charArrayOf('x', 'y', 'z', 'w')
	}
}

open class Vec2FieldEditor(parent: GameObject,
	field: Vec2Field,
	
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Vec2, Vec2Field>(parent, field, origin, size, callback, 2, ::Vec2, Vec2::get, Vec2::set)

open class Vec3FieldEditor(
	parent: GameObject,
	field: Vec3Field,
	
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Vec3, Vec3Field>(parent, field, origin, size, callback, 3, ::Vec3, Vec3::get, Vec3::set)

open class Vec4FieldEditor(
	parent: GameObject,
	field: Vec4Field,
	
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Vec4, Vec4Field>(parent, field, origin, size, callback, 4, ::Vec4, Vec4::get, Vec4::set)

open class QuatFieldEditor(
	parent: GameObject,
	field: QuatField,
	
	origin: Vec2,
	size: Vec2,
	callback: (String, String) -> Unit
) : VecFieldEditor<Quat, QuatField>(parent, field, origin, size, callback, 4, ::Quat, Quat::get, Quat::set)

fun <F: ComponentField<*>> createEditor(parent: GameObject, field: F, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): FieldEditor<*, *>?{
	return when(field){
		is BoolField -> BoolFieldEditor(parent, field, origin, size, callback)
		is IntField -> IntFieldEditor(parent, field, origin, size, callback)
		is IntRangeField -> IntRangeFieldEditor(parent, field, origin, size, callback)
		is UIntField -> UIntFieldEditor(parent, field, origin, size, callback)
		is FloatField -> FloatFieldEditor(parent, field, origin, size, callback)
		is DoubleField -> DoubleFieldEditor(parent, field, origin, size, callback)
		is Vec2Field -> Vec2FieldEditor(parent, field, origin, size, callback)
		is Vec3Field -> Vec3FieldEditor(parent, field, origin, size, callback)
		is Vec4Field -> Vec4FieldEditor(parent, field, origin, size, callback)
		is QuatField -> QuatFieldEditor(parent, field, origin, size, callback)
		is ShaderField -> ShaderFieldEditor(parent, field, origin, size, callback)
		is TextureField -> TextureFieldEditor(parent, field, origin, size, callback)
		is ModelField -> ModelFieldEditor(parent, field, origin, size, callback)
		else -> null
	}
}

