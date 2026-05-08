package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.bool
import com.pineypiney.game_engine.util.serialisation.Codec
import glm_.int
import glm_.quat.Quat
import glm_.vec3.Vec3
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ComponentFieldEdit(override val obj: GameObject, screen: EditorScreen, val fieldKey: String, val oldVal: ByteArray, val newVal: ByteArray) : ObjectEdit(screen) {

	override fun undo() {
		set(oldVal.inputStream())
	}

	override fun redo() {
		set(newVal.inputStream())
	}

	fun set(value: InputStream) {
		if(fieldKey.length > 1) {
			val (_, field) = obj.getComponentAndField(fieldKey) ?: return
			field.set(value)
		}
		else when(fieldKey[0]) {
			'n' -> obj.name = Codec.STRING.decode(value)
			'l' -> obj.layer = value.int()
			'a' -> obj.active = value.bool()
		}

		if(screen.editingObject == obj){
			screen.componentBrowser.refreshField(fieldKey)
		}
	}

	companion object {
		fun moveEdit(obj: GameObject, screen: EditorScreen, oldVal: Vec3, newVal: Vec3): ComponentFieldEdit{
			val oldPosStream = ByteArrayOutputStream(12)
			val newPosStream = ByteArrayOutputStream(12)
			Codec.VEC3.encode(oldPosStream, oldVal)
			Codec.VEC3.encode(newPosStream, newVal)
			return ComponentFieldEdit(
				obj, screen, "TransformComponent.position",
				oldPosStream.toByteArray(),
				newPosStream.toByteArray()
			)
		}
		fun rotateEdit(obj: GameObject, screen: EditorScreen, oldVal: Quat, newVal: Quat): ComponentFieldEdit{
			val oldRotStream = ByteArrayOutputStream(16)
			val newRotStream = ByteArrayOutputStream(16)
			Codec.QUAT.encode(oldRotStream, oldVal)
			Codec.QUAT.encode(newRotStream, newVal)
			return ComponentFieldEdit(
				obj, screen, "TransformComponent.rotation",
				oldRotStream.toByteArray(),
				newRotStream.toByteArray()
			)
		}
		fun scaleEdit(obj: GameObject, screen: EditorScreen, oldVal: Vec3, newVal: Vec3): ComponentFieldEdit{
			val oldSclStream = ByteArrayOutputStream(12)
			val newSclStream = ByteArrayOutputStream(12)
			Codec.VEC3.encode(oldSclStream, oldVal)
			Codec.VEC3.encode(newSclStream, newVal)
			return ComponentFieldEdit(
				obj, screen, "TransformComponent.scale",
				oldSclStream.toByteArray(),
				newSclStream.toByteArray()
			)
		}
	}
}