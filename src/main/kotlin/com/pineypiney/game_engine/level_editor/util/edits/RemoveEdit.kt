package com.pineypiney.game_engine.level_editor.util.edits

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D

class RemoveEdit(level: com.pineypiney.game_engine.level_editor.LevelMakerScreen, val removed: GameObject): Edit(level) {

    init {
        PixelEngine.logger.debug("Remove Edit Made")
    }

    override fun undo() {
        level.add(removed)
        if(removed is RenderedGameObject2D){
            removed.shader = RenderedGameObject2D.defaultShader
        }
    }

    override fun redo() {
        level.remove(removed)
    }
}