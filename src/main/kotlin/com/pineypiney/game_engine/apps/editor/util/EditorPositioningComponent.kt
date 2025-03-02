package com.pineypiney.game_engine.apps.editor.util

import glm_.vec3.Vec3

interface EditorPositioningComponent : EditComponent{

	fun place(oldPos: Vec3, newPos: Vec3): Vec3
}