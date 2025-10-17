package com.pineypiney.game_engine.apps.editor.util

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.CursorPosition
import glm_.vec2.Vec2
import glm_.vec3.Vec3

interface Draggable {

	fun getElement(): Any
	fun addRenderer(parent: GameObject, cursor: CursorPosition)
	fun position(obj: GameObject, cursorPos: Vec2, scenePosition: CursorPosition){
		obj.position = Vec3(cursorPos, obj.position.z)
	}

}