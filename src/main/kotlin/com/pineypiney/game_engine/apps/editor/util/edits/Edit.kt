package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.Deletable

abstract class Edit(val screen: EditorScreen) : Deletable {

	abstract fun undo()
	abstract fun redo()
	override fun delete() {}
}