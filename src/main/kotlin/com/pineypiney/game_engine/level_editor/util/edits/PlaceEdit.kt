package com.pineypiney.game_engine.level_editor.util.edits

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D

class PlaceEdit(level: com.pineypiney.game_engine.level_editor.LevelMakerScreen, val placed: GameObject): Edit(level) {

    init {
        PixelEngine.logger.debug("Place Edit Made")
    }

    override fun undo() {
        level.remove(placed)
    }

    override fun redo() {
        level.add(placed)
        if(placed is RenderedGameObject2D){
            placed.shader = RenderedGameObject2D.defaultShader
        }
    }
}