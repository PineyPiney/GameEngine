package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.extension_functions.toString
import glm_.quat.Quat
import glm_.vec3.Vec3

class ComponentFieldEdit(obj: GameObject, screen: EditorScreen, val field: String, val oldVal: String, val newVal: String) : ObjectEdit(obj, screen) {

	override fun undo() {
		obj.setProperty(field, oldVal)
	}

	override fun redo() {
		obj.setProperty(field, newVal)
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