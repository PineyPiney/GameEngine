package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.apps.editor.object_browser.ObjectNode
import com.pineypiney.game_engine.apps.editor.util.DraggableAcceptor
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.components.fields.*
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.components.widgets.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.widgets.CheckBoxComponent
import com.pineypiney.game_engine.objects.components.widgets.DropdownComponent
import com.pineypiney.game_engine.objects.components.widgets.TextFieldComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.ActionIntSliderComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.slider.BasicActionSlider
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.capitalise
import com.pineypiney.game_engine.util.maths.shapes.Parallelogram
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
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
import java.util.function.Consumer
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.reflect.KClass

abstract class FieldEditor<T, out F : ComponentField<T>>(
	parent: GameObject,
	val field: F,
	val component: ComponentI,
	position: Vec2i,
	size: Vec2i
) : DefaultInteractorComponent(parent), DraggableAcceptor {

	init {
		parent.components.add(PixelTransformComponent(parent, position - Vec2i(0, size.y), size, Vec2(0f, 1f)))
		passThrough = true
	}

	override var canDrop: Boolean = false

	override fun init() {
		super.init()
		createChildren()
	}

	abstract fun createChildren()

	abstract fun update(scale: Int)

	fun createText(text: String = field.id.substringAfterLast('.').capitalise(), alignment: Int = Text.ALIGN_CENTER_RIGHT, pos: Vec2 = Vec2(0f), size: Vec2 = Vec2(.25f, 1f)) =
		Text.makeMenuText(text, Vec4(0f, 0f, 0f, 1f), 16, alignment).apply { position = Vec3(pos, .01f); scale = Vec3(size, 1f) }

	companion object {
		init {
			addEditor(BoolField::class, ::BoolFieldEditor)
		}

		fun getHeight(pixelHeight: Int, count: Int, space: Float = .1f): Int{
			val pixelSpace = ceil(pixelHeight * space).toInt()
			return pixelHeight * count + ((count - 1) * pixelSpace)
		}
	}
}

open class DefaultFieldEditor<T, F : ComponentField<T>>(parent: GameObject, f: F, component: ComponentI, position: Vec2i, size: Vec2i, callback: (T, T) -> Unit) :
	FieldEditor<T, F>(parent, f, component, position, size) {

	val nameText = createText()

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Text Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f)) { textField, _, _ ->
		try {
			val oldSer = field.getter()
			val value = field.parse(this.component, textField.text)
			if(value != null) {
				field.setter(value)
				callback(oldSer, value)
			}
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
	}

	override fun update(scale: Int) {
		textField.text = field.serialise(this.component, field.getter())
	}
}

open class BoolFieldEditor(parent: GameObject, f: BoolField, component: ComponentI, position: Vec2i, size: Vec2i, callback: (Boolean, Boolean) -> Unit) :
	FieldEditor<Boolean, BoolField>(parent, f, component, position, size), UpdatingAspectRatioComponent{

	val nameText = createText()

	val checkBox = CheckBoxComponent.createCheckBox("Bool Field '${field.id}' Checkbox", field.getter()){
		field.setter(it)
		callback(!it, it)
	}.second

	override fun createChildren() {
		checkBox.parent.pixel(Vec2i(0), Vec2i(16), Vec2(.27, 0f))
		parent.addChild(nameText, checkBox.parent)
//		asp()
	}

	override fun update(scale: Int) {
		checkBox.ticked = field.getter()
	}

	override fun updateAspectRatio(view: Viewport) {
//		asp()
	}

	fun asp(){
		checkBox.parent.scale = Vec3(parent.transformComponent.worldScale.run { y / x }, 1f, 1f)
	}
}

open class PrimitiveFieldEditor<T: Any, F: ComponentField<T>>(parent: GameObject, f: F, component: ComponentI, position: Vec2i, size: Vec2i, parse: (String) -> T, callback: (T, T) -> Unit) :
	FieldEditor<T, F>(parent, f, component, position, size){

	val nameText = createText()

	val textField = ActionTextField<TextFieldComponent>("Number Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f), field.getter().toString(), 16) { textField, _, _ ->
		try {
			val value = parse(textField.text)
			val oldVal = field.getter()
			field.setter(value)
			callback(oldVal, value)
		} catch (_: NumberFormatException) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
	}

	override fun update(scale: Int) {
		textField.text = field.getter().toString()
	}
}

open class IntFieldEditor(parent: GameObject, field: IntField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Int, Int) -> Unit):
	PrimitiveFieldEditor<Int, IntField>(parent, field, component, origin, size, Integer::parseInt, callback)

open class UIntFieldEditor(parent: GameObject, field: UIntField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (UInt, UInt) -> Unit):
	PrimitiveFieldEditor<UInt, UIntField>(parent, field, component, origin, size, { it.toUIntOrNull() ?: 0u }, callback)

open class FloatFieldEditor(parent: GameObject, field: FloatField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Float, Float) -> Unit):
	PrimitiveFieldEditor<Float, FloatField>(parent, field, component, origin, size, java.lang.Float::parseFloat, callback)

open class DoubleFieldEditor(parent: GameObject, field: DoubleField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Double, Double) -> Unit):
	PrimitiveFieldEditor<Double, DoubleField>(parent, field, component, origin, size, java.lang.Double::parseDouble, callback)

open class IntRangeFieldEditor(parent: GameObject, f: IntRangeField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Int, Int) -> Unit)
	: FieldEditor<Int, IntRangeField>(parent, f, component, origin, size){

	val nameText = createText()

	val slider = BasicActionSlider("Int Slider", Vec2(.27f, 0f), Vec2(.7f, 1f), field.range.first, field.range.last, field.getter()){
		val oldVal = field.getter()
		field.setter(it.value)
		callback(oldVal, it.value)

	}

	override fun createChildren() {
		parent.addChild(nameText, slider)
	}

	override fun update(scale: Int) {
		slider.getComponent<ActionIntSliderComponent>()?.value = field.getter()
	}

}

open class ShaderFieldEditor(parent: GameObject, f: ShaderField, component: ComponentI, origin: Vec2i, size: Vec2i, val callback: (Shader, Shader) -> Unit)
	: FieldEditor<Shader, ShaderField>(parent, f, component, origin, Vec2i(size.x, getHeight(size.y, 3))) {

	val vertexText = createText("Vertex", pos = Vec2(0f, .7f), size = Vec2(.25f, .3f))
	val vertexField = ActionTextField<ActionTextFieldComponent<*>>("Vertex Field", Vec3(.27f, .7f, 0f), Vec2(.7f, .3f), field.getter().vName, 16) { _, _, _ ->
		updateValue()
	}
	val fragmentText = createText("Fragment", pos = Vec2(0f, .35f), size = Vec2(.25f, .3f))
	val fragmentField = ActionTextField<ActionTextFieldComponent<*>>("Fragment Field", Vec3(.27f, .35f, 0f), Vec2(.7f, .3f), field.getter().fName, 16) { _, _, _ ->
		updateValue()
	}
	val geometryText = createText("Geometry", pos = Vec2(0f, .0f), size = Vec2(.25f, .3f))
	val geometryField = ActionTextField<ActionTextFieldComponent<*>>("Geometry Field", Vec3(.27f, 0f, 0f), Vec2(.7f, .3f), field.getter().gName ?: "", 16) { _, _, _ ->
		updateValue()
	}

	fun updateValue(){
		try {
			val newS = if(geometryField.text.isEmpty()) ShaderLoader[ResourceKey(vertexField.text), ResourceKey(fragmentField.text)]
			else ShaderLoader[ResourceKey(vertexField.text), ResourceKey(fragmentField.text), ResourceKey(geometryField.text)]
			val oldVal = field.getter()
			field.setter(newS)
			callback(oldVal, newS)
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(vertexText, vertexField, fragmentText, fragmentField, geometryText, geometryField)
	}

	override fun update(scale: Int) {
		val s = field.getter()
		vertexField.text = s.vName
		fragmentField.text = s.fName
		geometryField.text = s.gName ?: ""
	}

	override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
		if(element is File){
			val fields = arrayOf(vertexField, fragmentField, geometryField)
			val relCur = (cursorPos.y - parent.transformComponent.worldPosition.y) / parent.transformComponent.worldScale.y
			val ext = element.extension.lowercase()
			val m = when (ext) {
				"vs" if relCur >= .67f -> 0
				"fs" if relCur in .33f.. .67f -> 1
				"gs" if relCur <= .33f -> 2
				else -> -1
			}

			for((i, textField) in fields.withIndex()) {
				val renderer: ColourRendererComponent = textField.getComponent<ColourRendererComponent>() ?: continue
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

open class TextureFieldEditor(parent: GameObject, f: TextureField, component: ComponentI, origin: Vec2i, size: Vec2i, val callback: (Texture, Texture) -> Unit)
	: FieldEditor<Texture, TextureField>(parent, f, component, origin, size) {

	val nameText = createText()

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Texture Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f), field.serialise(component, field.getter()), 16) { textField, _, _ ->
		try {
			updateValue()
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
	}

	override fun update(scale: Int) {
		textField.text = field.serialise(component, field.getter())
	}

	fun updateValue(){
		val oldValue = field.getter()
		val value = TextureLoader[ResourceKey(textField.text)]
		field.setter(value)
		callback(oldValue, value)
	}

	override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
		val renderer = textField.getComponent<ColourRendererComponent>()
		val willAccept = element is File && element.extension.lowercase() == "png"
		renderer?.colour?.xyz = if(willAccept) Vec3(0.65f) else Vec3(.5f)
		return willAccept
	}

	override fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen) {
		if(element is File){
			val path = element.path.replace(s, '/').removePrefix(screen.gameEngine.resourcesLoader.location + '/' + screen.gameEngine.resourcesLoader.textureLocation).substringBefore('.')
			textField.text = path
			updateValue()
		}
	}
}

open class ModelFieldEditor(parent: GameObject, f: ModelField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Model, Model) -> Unit)
	: FieldEditor<Model, ModelField>(parent, f, component, origin, size) {

	val nameText = createText()

	val textField = ActionTextField<ActionTextFieldComponent<*>>("Model Field", Vec3(.27f, 0f, 0f), Vec2(.7f, 1f), field.serialise(component, field.getter()), 16) { textField, _, _ ->
		try {
			val value = ModelLoader[ResourceKey(textField.text)]
			val oldVal = field.getter()
			field.setter(value)
			callback(oldVal, value)
		} catch (_: Exception) {

		}
	}

	override fun createChildren() {
		parent.addChild(nameText, textField)
	}

	override fun update(scale: Int) {
		textField.text = field.serialise(component, field.getter())
	}

	override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
		val renderer = textField.getComponent<ColourRendererComponent>()
		val willAccept = element is File && arrayOf("glb", "gltf", "pgm", "vox").contains(element.extension.lowercase())
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


open class Shape2DFieldEditor(parent: GameObject, f: Shape2DField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Shape2D, Shape2D) -> Unit)
	: FieldEditor<Shape2D, Shape2DField>(
	parent,
	f, component,
	origin,
	Vec2i(size.x, getHeight(size.y, 1 + (ShapeType.getByClass(f.getter()::class)?.numFields ?: 0)))
) {

	var height = size.y
	val nameText = createText()

	val dropdown = DropdownComponent.createDropdownMenu("Shape Field", ShapeType.entries.map { it.id }.toTypedArray(), 4f, Text.Params().withFontSize(16)){ menu, selected ->
		val shape = when(selected) {
			"Rectangle" -> Rect2D(Vec2(0f), 1f, 1f)
			"Parallelogram" -> Parallelogram(Vec2(0f), Vec2(-1f, 1f), Vec2(1f))
			else -> null
		}
		if(shape != null){
			val oldShape = field.getter()
			field.setter(shape)
			ShapeType.getByClass(shape::class)?.let { updateShapeFields(it, height) }
			callback(oldShape, shape)
		}
	}

	val fieldsContainer = GameObject("Shape Fields", 1)

	override fun createChildren() {
		dropdown.os(Vec2(.27f, 0f), Vec2(.7f, 1f))
		dropdown.getChild("Shape Field List")?.position = Vec3(0f, -4.3f, .2f)
		parent.addChild(nameText, dropdown, fieldsContainer)
	}

	override fun update(scale: Int) {
		height = scale
		val value = field.getter()
		val type = ShapeType.getByClass(value::class) ?: return
		dropdown.getComponent<DropdownComponent>()?.setSelected(type.id)
		updateShapeFields(type, height)
	}

	fun updateShapeFields(type: ShapeType, scale: Int){
		val transform = parent.getComponent<PixelTransformComponent>() ?: return
		val dropdownYBonus = (scale * .25f).roundToInt()

		val totalHeight = getHeight(scale, type.numFields + 1) + dropdownYBonus
		transform.pixelScale = Vec2i(transform.pixelScale.x, totalHeight)

		dropdown.scale = Vec3(.7f, (scale + dropdownYBonus).toFloat() / totalHeight, 1f)
		dropdown.position = Vec3(.27f, 1f - dropdown.scale.y, 0f)
		nameText.scale = Vec3(.25f, dropdown.scale.y, 1f)
		nameText.position = Vec3(0f, dropdown.position.y, 0f)

		val fieldsHeight =  getHeight(scale, type.numFields)
		fieldsContainer.scale = Vec3(1f, fieldsHeight.toFloat() / totalHeight, 1f)
		type.addFields(fieldsContainer, scale, field.getter, field.setter)
	}

	enum class ShapeType(val id: String, val klass: KClass<*>, val numFields: Int, val addFields: GameObject.(Int, Function0<Shape2D>, Consumer<Shape2D>) -> Unit) {
		RECT("Rectangle", Rect2D::class, 3, e@{ h, getter, consumer ->

			fun getRect(): Rect2D? {
				val shape = getter() as? Rect2D
				if (shape == null) {
					GameEngineI.logger.warn("ShapeType Rectangle in ShapeFieldEditor received shape type ${getter()::class}")
				}
				return shape
			}
			val shape = getRect() ?: return@e

			val posText = Text.makeMenuText("Position", Vec4(0f, 0f, 0f, 1f), 16, Text.ALIGN_CENTER_RIGHT).apply { position = Vec3(0f, .7f, .01f); scale = Vec3(.25f, .3f, 1f) }
			val posXField = ActionTextField<ActionTextFieldComponent<*>>("Pos.x Field", Vec3(.27f, .7f, 0f), Vec2(.345f, .3f), shape.origin.x.toString(), h) { field, _, _ ->
				val rect = getRect() ?: return@ActionTextField
				consumer.accept(Rect2D(Vec2(field.text.toFloat(), rect.origin.y), rect.lengths, rect.angle))
			}
			val posYField = ActionTextField<ActionTextFieldComponent<*>>("Pos.y Field", Vec3(.625f, .7f, 0f), Vec2(.345f, .3f), shape.origin.y.toString(), h) { field, _, _ ->
				val rect = getRect() ?: return@ActionTextField
				consumer.accept(Rect2D(Vec2(rect.origin.x, field.text.toFloat()), rect.lengths, rect.angle))
			}

			val scaleText = Text.makeMenuText("Scale", Vec4(0f, 0f, 0f, 1f), 16, Text.ALIGN_CENTER_RIGHT).apply { position = Vec3(0f, .35f, .01f); scale = Vec3(.25f, .3f, 1f) }
			val scaleXField = ActionTextField<ActionTextFieldComponent<*>>("Scale.x Field", Vec3(.27f, .35f, 0f), Vec2(.345f, .3f), shape.length1.toString(), h) { field, _, _ ->
				val rect = getRect() ?: return@ActionTextField
				consumer.accept(Rect2D(rect.origin, field.text.toFloat(), rect.length2, rect.angle))
			}
			val scaleYField = ActionTextField<ActionTextFieldComponent<*>>("Scale.y Field", Vec3(.625f, .35f, 0f), Vec2(.345f, .3f), shape.length2.toString(), h) { field, _, _ ->
				val rect = getRect() ?: return@ActionTextField
				consumer.accept(Rect2D(rect.origin, rect.length1, field.text.toFloat(), rect.angle))
			}


			val angleText = Text.makeMenuText("Angle", Vec4(0f, 0f, 0f, 1f), 16, Text.ALIGN_CENTER_RIGHT).apply { position = Vec3(0f, .0f, .01f); scale = Vec3(.25f, .3f, 1f) }
			val angleField = ActionTextField<ActionTextFieldComponent<*>>("Angle Field", Vec3(.27f, 0f, 0f), Vec2(.7f, .3f), shape.angle.toString(), h) { field, _, _ ->
				val rect = getRect() ?: return@ActionTextField
				consumer.accept(Rect2D(rect.origin, rect.lengths, field.text.toFloat()))
			}

			deleteAllChildren()
			addChild(posText, posXField, posYField, scaleText, scaleXField, scaleYField, angleText, angleField)
			init()
		}),
		PARALLELOGRAM("Parallelogram", Parallelogram::class, 3, e@{ h, getter, consumer ->

			fun getPara(): Parallelogram? {
				val shape = getter() as? Parallelogram
				if (shape == null) {
					GameEngineI.logger.warn("ShapeType Parallelogram in ShapeFieldEditor received shape type ${getter()::class}")
				}
				return shape
			}
			val shape = getPara() ?: return@e

			val posText = Text.makeMenuText("Position", Vec4(0f, 0f, 0f, 1f), 16, Text.ALIGN_CENTER_RIGHT).apply { position = Vec3(0f, .7f, .01f); scale = Vec3(.25f, .3f, 1f) }
			val posXField = ActionTextField<ActionTextFieldComponent<*>>("Pos.x Field", Vec3(.27f, .7f, 0f), Vec2(.345f, .3f), shape.origin.x.toString(), h) { field, _, _ ->
				val para = getPara() ?: return@ActionTextField
				consumer.accept(Parallelogram(Vec2(field.text.toFloat(), para.origin.y), para.side1, para.side2))
			}
			val posYField = ActionTextField<ActionTextFieldComponent<*>>("Pos.y Field", Vec3(.625f, .7f, 0f), Vec2(.345f, .3f), shape.origin.y.toString(), h) { field, _, _ ->
				val para = getPara() ?: return@ActionTextField
				consumer.accept(Parallelogram(Vec2(para.origin.x, field.text.toFloat()), para.side1, para.side2))
			}

			val side1Text = Text.makeMenuText("Side1", Vec4(0f, 0f, 0f, 1f), 16, Text.ALIGN_CENTER_RIGHT).apply { position = Vec3(0f, .35f, .01f); scale = Vec3(.25f, .3f, 1f) }
			val side1XField = ActionTextField<ActionTextFieldComponent<*>>("Side1.x Field", Vec3(.27f, .35f, 0f), Vec2(.345f, .3f), shape.side1.x.toString(), h) { field, _, _ ->
				val para = getPara() ?: return@ActionTextField
				consumer.accept(Parallelogram(para.origin, Vec2(field.text.toFloat(), para.side1.y), para.side2))
			}
			val side1YField = ActionTextField<ActionTextFieldComponent<*>>("Side1.y Field", Vec3(.625f, .35f, 0f), Vec2(.345f, .3f), shape.side1.y.toString(), h) { field, _, _ ->
				val para = getPara() ?: return@ActionTextField
				consumer.accept(Parallelogram(para.origin, Vec2(para.side1.x, field.text.toFloat()), para.side2))
			}


			val side2Text = Text.makeMenuText("Side2", Vec4(0f, 0f, 0f, 1f), 16, Text.ALIGN_CENTER_RIGHT).apply { position = Vec3(0f, .0f, .01f); scale = Vec3(.25f, .3f, 1f) }
			val side2XField = ActionTextField<ActionTextFieldComponent<*>>("Side2.x Field", Vec3(.27f, 0f, 0f), Vec2(.345f, .3f), shape.side2.x.toString(), h) { field, _, _ ->
				val para = getPara() ?: return@ActionTextField
				consumer.accept(Parallelogram(para.origin, para.side1, Vec2(field.text.toFloat(), para.side2.y)))
			}
			val side2YField = ActionTextField<ActionTextFieldComponent<*>>("Side2.y Field", Vec3(.625f, 0f, 0f), Vec2(.345f, .3f), shape.side2.y.toString(), h) { field, _, _ ->
				val para = getPara() ?: return@ActionTextField
				consumer.accept(Parallelogram(para.origin, para.side1, Vec2(para.side2.x, field.text.toFloat())))
			}

			deleteAllChildren()
			addChild(posText, posXField, posYField, side1Text, side1XField, side1YField, side2Text, side2XField, side2YField)
			init()
		});

		companion object {
			fun getByClass(klass: KClass<*>): ShapeType? {
				return entries.firstOrNull { it.klass == klass }
			}
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
	f: F,
	component: ComponentI,
	origin: Vec2i,
	size: Vec2i,
	callback: (V, V) -> Unit,
	vecSize: Int,
	parseT: (String) -> T,
	copy: (V) -> V,
	val inGet: V.(Int) -> T,
	inSet: V.(Int, T) -> Unit
) : FieldEditor<V, F>(parent, f, component, origin, size) {

	val nameField = createText()

	val x = (.7f - ((vecSize - 1) * .01f)) / vecSize
	val textFields = Array<ActionTextField<*>>(vecSize) {
		ActionTextField<ActionTextFieldComponent<*>>(
			"Vec Field $it",
			Vec3(.27f + it * (x + .01f), 0f, 0f),
			Vec2(x, 1f),
			field.getter().inGet(it).toString(),
			16
		) { textField, _, _ ->
			try {
				val oldVal = field.getter()
				val newVal = copy(oldVal)
				newVal.inSet(it, parseT(textField.text))
				field.setter(newVal)
				callback(oldVal, newVal)
				textField.text = field.getter().inGet(it).toString()
			} catch (_: NumberFormatException) {

			}
		}.also { textField -> textField.name = "${parent.name.substring(0, parent.name.length - 13)}.${names[it]} Field Editor" }
	}

	override fun createChildren() {
		parent.addChild(nameField, *textFields)
	}

	override fun update(scale: Int) {
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

open class VeciFieldEditor<V, out F : ComponentField<V>>(parent: GameObject, field: F, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (V, V) -> Unit, vecSize: Int, copy: (V) -> V, inGet: V.(Int) -> Int, inSet: V.(Int, Int) -> Unit):
	VecTFieldEditor<Int, V, F>(parent, field, component, origin, size, callback, vecSize, Integer::parseInt, copy, inGet, inSet)

open class Vec2iFieldEditor(parent: GameObject, field: Vec2iField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Vec2i, Vec2i) -> Unit) :
	VeciFieldEditor<Vec2i, Vec2iField>(parent, field, component, origin, size, callback, 2, ::Vec2i, Vec2i::get, Vec2i::set)

open class Vec3iFieldEditor(parent: GameObject, field: Vec3iField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Vec3i, Vec3i) -> Unit) :
	VeciFieldEditor<Vec3i, Vec3iField>(parent, field, component, origin, size, callback, 3, ::Vec3i, Vec3i::get, Vec3i::set)

open class Vec4iFieldEditor(parent: GameObject, field: Vec4iField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Vec4i, Vec4i) -> Unit) :
	VeciFieldEditor<Vec4i, Vec4iField>(parent, field, component, origin, size, callback, 4, ::Vec4i, Vec4i::get, Vec4i::set)


open class VecFieldEditor<V, out F : ComponentField<V>>(parent: GameObject, field: F, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (V, V) -> Unit, vecSize: Int, copy: (V) -> V, inGet: V.(Int) -> Float, inSet: V.(Int, Float) -> Unit) :
	VecTFieldEditor<Float, V, F>(parent, field, component, origin, size, callback, vecSize, java.lang.Float::parseFloat, copy, inGet, inSet)

open class Vec2FieldEditor(parent: GameObject, field: Vec2Field, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Vec2, Vec2) -> Unit) :
	VecFieldEditor<Vec2, Vec2Field>(parent, field, component, origin, size, callback, 2, ::Vec2, Vec2::get, Vec2::set)

open class Vec3FieldEditor(parent: GameObject, field: Vec3Field, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Vec3, Vec3) -> Unit) :
	VecFieldEditor<Vec3, Vec3Field>(parent, field, component, origin, size, callback, 3, ::Vec3, Vec3::get, Vec3::set)

open class Vec4FieldEditor(parent: GameObject, field: Vec4Field, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Vec4, Vec4) -> Unit) :
	VecFieldEditor<Vec4, Vec4Field>(parent, field, component, origin, size, callback, 4, ::Vec4, Vec4::get, Vec4::set)

open class QuatFieldEditor(parent: GameObject, field: QuatField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (Quat, Quat) -> Unit) :
	VecFieldEditor<Quat, QuatField>(parent, field, component, origin, size, callback, 4, ::Quat, Quat::get, Quat::set)

open class GameObjectFieldEditor(parent: GameObject, f: GameObjectField, component: ComponentI, origin: Vec2i, size: Vec2i, callback: (GameObject?, GameObject?) -> Unit)
	: FieldEditor<GameObject?, GameObjectField>(parent, f, component, origin, size) {

	val nameText = createText()
	val nameBackground = GameObject("GameObject Name Background", 1)
	val objName = createText(f.getter()?.name ?: "Null", Text.ALIGN_CENTER_LEFT, Vec2(0f), Vec2(1f))

	override fun createChildren() {
		parent.addChild(nameText, nameBackground)
		nameBackground.os(Vec2(.27f, 0f), Vec2(.7f, 1f))
		nameBackground.components.add(ColourRendererComponent(nameBackground, Vec3(.5f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))
		objName.translate(Vec3(0f, 0f, .01f))
		nameBackground.addChild(objName)
	}

	override fun update(scale: Int) {
		val value = field.getter()
		objName.getComponent<TextRendererComponent>()?.setTextContent(value?.name ?: "Null")
	}

	override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
		val renderer = nameBackground.getComponent<ColourRendererComponent>()
		val willAccept = element is ObjectNode
		renderer?.colour?.xyz = if(willAccept) Vec3(0.65f) else Vec3(.5f)
		return willAccept
	}

	override fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen) {
		if(element is ObjectNode){
			field.setter(element.obj)
			objName.getComponent<TextRendererComponent>()?.setTextContent(element.obj.name)
		}
	}
}

class EditorType<T, F: ComponentField<T>>(val klass: KClass<F>, val creator: EditorCreator<T, F>)

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
	EditorType(Shape2DField::class, ::Shape2DFieldEditor),
	EditorType(GameObjectField::class, ::GameObjectFieldEditor),
)

fun <T, F: ComponentField<T>> addEditor(klass: KClass<F>, creator: EditorCreator<T, F>){
	editors.add(EditorType(klass, creator))
}

@Suppress("UNCHECKED_CAST")
fun <T, F: ComponentField<T>> createEditor(parent: GameObject, field: F, component: ComponentI, position: Vec2i, size: Vec2i, callback: (T, T) -> Unit): FieldEditor<*, *> {
	val creator: EditorType<T, F> = (editors.firstOrNull { it.klass == field::class } as? EditorType<T, F>) ?: return DefaultFieldEditor(parent, field, component, position, size, callback)
	return creator.creator(parent, field, component, position, size, callback)
}

typealias EditorCreator<T, F> = (parent: GameObject, field: F, component: ComponentI, position: Vec2i, size: Vec2i, callback: (T, T) -> Unit) -> FieldEditor<*, F>

