package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject

class AddObjectEdit(override var obj: GameObject, val parent: GameObject?, screen: EditorScreen) : ObjectEdit(screen) {

	override fun undo() {
		screen.removeSceneObject(obj)
	}

	override fun redo() {
		screen.addSceneObject(obj, parent)
	}

	override fun delete() {
		if(obj.getObjectCollection() == null) obj.delete()
	}
}