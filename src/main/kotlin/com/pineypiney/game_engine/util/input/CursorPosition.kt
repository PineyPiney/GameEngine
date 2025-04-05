package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class CursorPosition(val position: Vec2, val screenSpace: Vec2, val pixels: Vec2i){

	constructor(position: Vec2, window: WindowI): this(position, Vec2(position.x / window.aspectRatio, position.y), Vec2i((position.x + window.aspectRatio) * .5f * window.height, (position.y + 1f) * .5f * window.height))

	operator fun minus(other: CursorPosition): CursorPosition{
		return CursorPosition(position - other.position, screenSpace - other.screenSpace, pixels - other.pixels)
	}
}