package com.pineypiney.game_engine.resources.fonts

import com.pineypiney.game_engine.util.BitMask
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

sealed interface Glyph {

	data class Simple(val points: Array<Vec2>, val min: Vec2, val max: Vec2, val onCurve: BitMask, val contourEnds: IntArray) : Glyph {

		val size = max - min
		val contourPoints = compileContourPoints()
		val curves = compileCurves()

		fun compileContourPoints(): List<List<Vec2>> {

			val contours = mutableListOf<MutableList<Vec2>>()

			var firstPoint = 0
			for (lastPoint in contourEnds) {
				val contour = mutableListOf<Vec2>()
				contours.add(contour)

				// The first point is always on curve, either the first point,
				// The last point moved to the front or an implied on curve point
				if (!onCurve[firstPoint]) {
					if (onCurve[lastPoint]) contour.add(points[lastPoint])
					else contour.add((points[firstPoint] + points[lastPoint]) / 2)
				}

				for (i in firstPoint..<lastPoint) {
					contour.add(points[i])
					if (onCurve[i] == onCurve[i + 1]) {
						contour.add((points[i] + points[i + 1]) / 2)
					}
				}
				if (!onCurve[lastPoint]) contour.add(points[lastPoint])
				else if (onCurve[firstPoint]) {
					contour.add(points[lastPoint])
					contour.add((points[firstPoint] + points[lastPoint]) / 2)
				}

				firstPoint = lastPoint + 1
			}

			return contours
		}

		fun compileCurves(): List<BezierCurve> {
			val list = mutableListOf<BezierCurve>()
			for (c in contourPoints) {
				repeat(c.size shr 1) {
					list.add(BezierCurve(c[it * 2], c[it * 2 + 1], c[(it * 2 + 2) % c.size]))
				}
			}
			return list
		}

		override fun equals(other: Any?): Boolean {
			return super.equals(other)
		}

		override fun hashCode(): Int {
			return super.hashCode()
		}

		companion object {
			val EMPTY = Simple(emptyArray(), Vec2(0f), Vec2(0f), BitMask(0), intArrayOf())
		}
	}

	data class Compound(val children: Map<Int, Vec2i>) : Glyph

}