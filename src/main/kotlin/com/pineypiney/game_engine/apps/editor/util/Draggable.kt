package com.pineypiney.game_engine.apps.editor.util

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.CursorPosition
import glm_.vec2.Vec2

interface Draggable {

	fun getElement(): Any
	fun addRenderer(parent: GameObject)
	fun position(obj: GameObject, cursorPos: Vec2, scenePosition: CursorPosition)

}