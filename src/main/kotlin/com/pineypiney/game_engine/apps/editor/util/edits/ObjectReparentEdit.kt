package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject

class ObjectReparentEdit(screen: EditorScreen, override val obj: GameObject, val oldParent: GameObject?, val newParent: GameObject?): ObjectEdit(screen) {

	override fun undo() {
		screen.reparentSceneObject(obj, oldParent)
	}

	override fun redo() {
		screen.reparentSceneObject(obj, newParent)
	}
}