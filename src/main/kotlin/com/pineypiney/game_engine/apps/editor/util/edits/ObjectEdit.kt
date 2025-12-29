package com.pineypiney.game_engine.apps.editor.util.edits

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject

abstract class ObjectEdit(screen: EditorScreen) : Edit(screen) {
	abstract val obj: GameObject
}