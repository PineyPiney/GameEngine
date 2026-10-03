package com.pineypiney.game_engine.resources.fonts

import com.pineypiney.game_engine.util.extension_functions.round
import glm_.vec2.Vec2

class BezierCurve(val p1: Vec2, val p2: Vec2, val p3: Vec2) {

	val a: Vec2 = (p1 + p3 - (p2 * 2f)).round(.001f)
	val b: Vec2 = ((p2 - p1) * 2f).round(.001f)
	val c: Vec2 = (p1).round(.001f)

	val bisected = a.x == 0f && a.y == 0f
	val vertical = a.x == 0f && b.x == 0f
	val horizontal = a.y == 0f && b.y == 0f

	override fun toString(): String {
		val s = StringBuilder("Curve($p1, $p2, $p3)[")
		if (bisected) s.append("bisected,")
		if (vertical) s.append("vertical,")
		if (horizontal) s.append("horizontal")
		return s.append(']').toString()
	}
}