package com.pineypiney.game_engine.apps

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.RelativeTransformComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.init
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class ComponentEditor(
	var o: GameObject,
	component: ComponentI,
	origin: Vec2,
	size: Vec2,
	val callback: (String, String) -> Unit
) : MenuItem("Component Editor") {

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
		val flds = editingComponent::class.supertypes
		var i = editingComponent.fields.size
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
		for (f in editingComponent.fields) {
			addChild(
				f.editor(
					editingComponent,
					id + editingComponent.id + '.' + f.id,
					Vec2(0f, ((s * --i))),
					Vec2(1f, s),
					callback
				)
			)
		}
		children.init()
	}

	fun updateField(id: String) {
		val fe = children.filterIsInstance<Component.FieldEditor<*, *>>().firstOrNull { it.id == id }
		if (fe != null) fe.update()
		else GameEngineI.logger.warn("Could not find FieldEditor $id")
	}
}