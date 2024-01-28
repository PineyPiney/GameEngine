package com.pineypiney.game_engine.level_editor.util

import glm_.vec2.Vec2

data class ClickState(val button: Int, val state: Int, val time: Double, val pos: Vec2) {
}