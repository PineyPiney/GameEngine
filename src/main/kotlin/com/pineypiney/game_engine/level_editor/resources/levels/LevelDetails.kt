package com.pineypiney.game_engine.level_editor.resources.levels

import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec3.Vec3
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LevelDetails(val worldName: String, val fileName: String, val width: Float, val colour: Vec3, val icon: Texture, val created: LocalDateTime, val edited: LocalDateTime) {

    companion object{
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy")
    }
}