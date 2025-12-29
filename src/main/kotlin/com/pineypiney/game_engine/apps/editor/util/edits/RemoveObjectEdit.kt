package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject

class RemoveObjectEdit(override var obj: GameObject, val parent: GameObject?, screen: EditorScreen) : ObjectEdit(screen) {

	override fun undo() {
		screen.addSceneObject(obj, parent)
	}

	override fun redo() {
		screen.removeSceneObject(obj)
	}

	override fun delete() {
		if(obj.getObjectCollection() == null) obj.delete()
	}
}