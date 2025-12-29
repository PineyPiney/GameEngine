package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.apps.editor.FieldEditor
import com.pineypiney.game_engine.apps.editor.createEditor
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.RelativeTransformComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.objects.components.getAllFieldsExt
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.init
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class ComponentEditor(
	var o: GameObject,
	component: ComponentI,
	origin: Vec2,
	size: Vec2,
	val callback: (String, String, String) -> Unit
) : GameObject("Component Editor", 1) {

	var editingComponent: ComponentI = component
		set(value) {
			field = value
			children.delete()
			removeChildren(children)
			generateFields()
		}

	override var name: String = "Component Editor"

    init {
        components.add(RelativeTransformComponent(this, origin, size))
    }

	override fun init() {
		super.init()

		generateFields()
	}

	fun generateFields() {
		val fields = editingComponent.getAllFieldsExt()
		var i = fields.size
		val h = 0.06f

		position = Vec3(position.x, -0.5f * h * i, 0f)
		scale = Vec3(scale.x, h * i, 1f)

		var id = ""
		var p = editingComponent.parent
		while (p != o) {
			id = p.name + '.' + id
			p = p.parent ?: break
		}

		val s = 1f / i
		for (f in fields) {
			generateField(id, f, s, --i)
		}
		children.init()
	}

	fun <T, F: ComponentField<T>> generateField(id: String, f: F, s: Float, i: Int){
		val fieldID = id + editingComponent.id + '.' + f.id
		addChild(createEditor(GameObject("Field Editor $fieldID", 1),
			f, editingComponent, Vec2i(0f, ((s * i))), Vec2i(1f, s)
		){ ov, v ->
			callback(f.id, f.serialise(editingComponent, ov), f.serialise(editingComponent, v))
		}.applied().parent)
	}

	@Suppress("FilterIsInstanceResultIsAlwaysEmpty")
	fun updateField(id: String) {
		val fe = children.filterIsInstance<FieldEditor<*, *>>().firstOrNull { it.field.id == id }
		if (fe != null) fe.update(16)
		else GameEngineI.logger.warn("Could not find FieldEditor $id")
	}
}