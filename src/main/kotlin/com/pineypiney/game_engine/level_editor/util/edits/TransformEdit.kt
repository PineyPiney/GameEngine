package com.pineypiney.game_engine.level_editor.util.edits

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D

class TransformEdit(level: com.pineypiney.game_engine.level_editor.LevelMakerScreen, val placed: GameObject2D, val oldTransform: Transform2D, val newTransform: Transform2D): Edit(level) {

    init {
        PixelEngine.logger.debug("Transform Edit Made")
    }

    override fun undo() {
        oldTransform copyInto placed.transform
    }

    override fun redo() {
        newTransform copyInto placed.transform
    }
}

infix fun Transform2D.copyInto(other: Transform2D){
    other.position = position
    other.rotation = rotation
    other.scale = scale
}