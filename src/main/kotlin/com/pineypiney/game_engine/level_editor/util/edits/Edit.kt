package com.pineypiney.game_engine.level_editor.util.edits

import com.pineypiney.game_engine.level_editor.LevelMakerScreen

abstract class Edit(val level: LevelMakerScreen) {

    abstract fun undo()
    abstract fun redo()
}