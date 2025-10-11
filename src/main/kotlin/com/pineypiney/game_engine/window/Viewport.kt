package com.pineypiney.game_engine.window

import glm_.vec2.Vec2i

class Viewport(val bl: Vec2i, val tr: Vec2i) {

	constructor(left: Int, bottom: Int, right: Int, top: Int): this(Vec2i(left, bottom), Vec2i(right, top))

	val size = tr - bl
	val aspectRatio = size.x.toFloat() / size.y
	val inverseAspectRatio = size.y.toFloat() / size.x

	operator fun component1() = bl
	operator fun component2() = tr
	operator fun component3() = size
}