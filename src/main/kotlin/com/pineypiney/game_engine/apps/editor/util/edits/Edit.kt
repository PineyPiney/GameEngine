package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.Deleteable

abstract class Edit(val screen: EditorScreen): Deleteable {

	abstract fun undo()
	abstract fun redo()
	override fun delete() {}
}