package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.util.extension_functions.popFirstOrNull

class EditManager {

	private val undoList = mutableSetOf<Edit>()
	private val redoList = mutableSetOf<Edit>()

	fun undo(){
		val edit = undoList.popFirstOrNull { it == undoList.last() } ?: return
		edit.undo()
		redoList.add(edit)
	}

	fun redo(){
		val edit = redoList.popFirstOrNull { it == redoList.last() } ?: return
		edit.redo()
		undoList.add(edit)
	}

	fun addEdit(edit: Edit){
		undoList.add(edit)
		redoList.clear()
	}
}