package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.extension_functions.toString
import glm_.parseInt
import glm_.quat.Quat
import glm_.vec3.Vec3

class ComponentFieldEdit(override val obj: GameObject, screen: EditorScreen, val fieldKey: String, val oldVal: String, val newVal: String) : ObjectEdit(screen) {

	override fun undo() {
		set(oldVal)
	}

	override fun redo() {
		set(newVal)
	}

	fun set(value: String){
		if(fieldKey.length > 1) {
			val (component, field) = obj.getComponentAndField(fieldKey) ?: return
			field.set(value, component)
		}
		else when(fieldKey[0]) {
			'n' -> obj.name = value
			'l' -> obj.layer = value.parseInt()
			'a' -> obj.active = value[0].code > 0
		}

		if(screen.editingObject == obj){
			screen.componentBrowser.refreshField(fieldKey)
		}
	}

	companion object {
		fun moveEdit(obj: GameObject, screen: EditorScreen, oldVal: Vec3, newVal: Vec3): ComponentFieldEdit{
			return ComponentFieldEdit(
				obj, screen, "TransformComponent.position",
				oldVal.toString(",", ByteData::float2String),
				newVal.toString(",", ByteData::float2String)
			)
		}
		fun rotateEdit(obj: GameObject, screen: EditorScreen, oldVal: Quat, newVal: Quat): ComponentFieldEdit{
			return ComponentFieldEdit(
				obj, screen, "TransformComponent.rotation",
				oldVal.toString(",", ByteData::float2String),
				newVal.toString(",", ByteData::float2String)
			)
		}
		fun scaleEdit(obj: GameObject, screen: EditorScreen, oldVal: Vec3, newVal: Vec3): ComponentFieldEdit{
			return ComponentFieldEdit(
				obj, screen, "TransformComponent.scale",
				oldVal.toString(",", ByteData::float2String),
				newVal.toString(",", ByteData::float2String)
			)
		}
	}
}