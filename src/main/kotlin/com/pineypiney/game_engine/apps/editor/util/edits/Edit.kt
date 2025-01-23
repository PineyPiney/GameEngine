package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen

abstract class Edit(val scene: EditorScreen) {

	abstract fun undo()
	abstract fun redo()
}