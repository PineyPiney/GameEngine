package com.pineypiney.game_engine.util.maths.shapes

import com.pineypiney.game_engine.util.CollectionMap
import com.pineypiney.game_engine.util.Vectors
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.extension_functions.angleBetween
import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.extension_functions.transformedBy
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.abs

class Polygon(val vertices: List<Vec2>) : Shape2D() {

	override val min: Vec2
	override val max: Vec2

	init {
		val minMax = Vectors.minMaxVec2(vertices)
		min = minMax.first
		max = minMax.second
	}

	val triangles = triangulate()
	val convexPolygons = decomposeToConvexPolygons()
	val convex = convexPolygons.size == 1

	override fun intersectedBy(ray: Ray): Array<Vec3> {
		return tryTriangles { shape ->
			val intersection = shape.intersectedBy(ray)
			if (intersection.isNotEmpty()) intersection
			else null
		} ?: emptyArray()
	}

	override fun containsPoint(point: Vec2): Boolean {
		return anyTriangle { it.containsPoint(point) }
	}

	override fun vectorTo(point: Vec2): Vec2 {
		return triangles.minOfWith({ a, b -> a.length2().compareTo(b.length2()) }) { tri ->
			val triangle = createTriangle(tri)
			triangle.vectorTo(point)
		}
	}

	override fun getNormals(): Set<Vec2> {
		val set = mutableSetOf<Vec2>()
		for (i in vertices.indices) {
			val line = vertices[(i + 1) % vertices.size] - vertices[i]
			set.add(line.normal().normalize())
		}
		return set
	}

	override fun projectToNormal(normal: Vec2): Set<Float> {
		return projectAllPoints(normal, vertices)
	}

	override fun getBoundingCircle(): Circle {
		val rad = (max - min) / 2
		return Circle(min + rad, rad.length())
	}

	override fun getConvexPolygons(): Iterable<Shape2D> {
		return if (convex) listOf(this)
		else convexPolygons.map { indexes -> Polygon(indexes.map { vertices[it] }) }
	}

	override fun transformedBy(model: Mat4): Shape2D {
		return Polygon(vertices.map { it.transformedBy(model) })
	}

	fun createTriangle(indices: Triple<Int, Int, Int>) = Triangle(vertices[indices.first], vertices[indices.second], vertices[indices.third])

	fun withTriangles(func: (Triangle) -> Unit) {
		for (triangle in triangles) {
			func(Triangle(vertices[triangle.first], vertices[triangle.second], vertices[triangle.third]))
		}
	}

	fun <E> tryTriangles(func: (Triangle) -> E?): E? {
		for (triangle in triangles) {
			val result = func(Triangle(vertices[triangle.first], vertices[triangle.second], vertices[triangle.third]))
			if (result != null) return result
		}
		return null
	}

	fun anyTriangle(func: (Triangle) -> Boolean): Boolean {
		return triangles.any { triangle ->
			func(Triangle(vertices[triangle.first], vertices[triangle.second], vertices[triangle.third]))
		}
	}

	fun <E : Comparable<E>> minTriangle(func: (Triangle) -> E): E {
		return triangles.minOf { triangle ->
			func(Triangle(vertices[triangle.first], vertices[triangle.second], vertices[triangle.third]))
		}
	}

	// Ear clipping triangulation
	fun triangulate(): List<Triple<Int, Int, Int>> {
		val points = MutableList(vertices.size) { it }
		var pointIndex = 0
		var lastResolvedVertex = 0
		val triangles = mutableListOf<Triple<Int, Int, Int>>()
		point@ while (points.size > 3) {
			val prevPointIndex = if (pointIndex == 0) points.size - 1 else (pointIndex - 1) % points.size
			val nextPointIndex = (pointIndex + 1) % points.size

			val vertIndex = points[pointIndex]
			val prevIndex = points[prevPointIndex]
			val nextIndex = points[nextPointIndex]

			val vert = vertices[vertIndex]
			val prev = vertices[prevIndex]
			val next = vertices[nextIndex]

			val angle = (prev - vert).angleBetween(next - vert)
			// If this angle is reflex then this triangle is not an ear
			if (angle < 0.0) {
				pointIndex = (pointIndex + 1) % points.size
				if (lastResolvedVertex == pointIndex) break
				else continue
			}

			val triangle = Triangle(vert, prev, next)
			for (i in nextPointIndex + 1..nextPointIndex + points.size - 3) {
				val vertex = vertices[points[i % points.size]]
				if (triangle.containsPoint(vertex)) {
					pointIndex = (pointIndex + 1) % points.size
					if (lastResolvedVertex == pointIndex) break@point
					else continue@point
				}
			}

			triangles.add(Triple(prevIndex, vertIndex, nextIndex))
			points.removeAt(pointIndex)
			lastResolvedVertex = pointIndex
			if (pointIndex == points.size) pointIndex = 0
		}
		triangles.add(Triple(points[0], points[1], points[2]))
		return triangles
	}

	fun decomposeToConvexPolygons(): List<List<Int>> {
		val polygons = triangles.map { mutableListOf(it.first, it.second, it.third) }
		val lines = CollectionMap.set<Pair<Int, Int>, Int>()

		// Get all triangle lines and associate them with the triangles they form
		// Each line should not be part of more than 2 triangles
		for ((i, polygon) in polygons.withIndex()) {
			val polyLines = ShapeHelper.getLines(polygon)
			for (line in polyLines) lines.add(line, i)
		}

		// An array of indexes each pointing to a polygon,
		// This allows polygons to be deleted when they are merged,
		// and the lines will point to an index which will point to
		// the new polygon
		val polygonIndexes = IntArray(polygons.size) { it }
		val sharedLines = lines.filterValues { polys -> polys.size == 2 }.entries.toMutableList()

		// The current polygon whose lines are being checked
		var currentPolygon = -1

		while (sharedLines.isNotEmpty()) {

			// Get the next line to check, either the next line of the checking polygon
			// or the next line in the list
			val (line, polys) = if (currentPolygon == -1) {
				sharedLines.first()
			} else {
				val first = sharedLines.firstOrNull { (_, polys) -> polys.any { polygonIndexes[it] == currentPolygon } }
				if (first == null) {
					currentPolygon = -1
					sharedLines.first()
				} else first
			}

			val p0Index = polygonIndexes[polys.first()]
			val p1Index = polygonIndexes[polys.last()]
			val p0 = polygons[p0Index]
			val p1 = polygons[p1Index]

			if (
				checkJoiningAngle(p0, p1, line.first, vertices)
				|| checkJoiningAngle(p0, p1, line.second, vertices)
			) {
				// If removing this line would create a reflex angle then remove it from the list
				sharedLines.removeIf { it.key == line }
				continue
			}

			// If the angles at both ends of this line can be kept non-reflex, combine both polygons
			val p0Index0 = p0.indexOf(line.first)
			val p0Index1 = p0.indexOf(line.second)

			// If the indices of the consuming polygon are consecutive then
			// insert the new vertices in between
			val addAtEnd: List<Int>
			if (abs(p0Index0 - p0Index1) == 1) {
				// Copy the trailing vertices into addAtEnd
				// and clear them from the polygon
				val i = Math.max(p0Index0, p0Index1)
				val sublist = p0.subList(i, p0.size)
				addAtEnd = sublist.toList()
				sublist.clear()
			}
			// Otherwise the vertices of this line
			// are the first and last of the polygon and new vertices can be added onto the end
			else {
				addAtEnd = emptyList()
			}


			val p1Index0 = p1.indexOf(line.first)
			val p1Index1 = p1.indexOf(line.second)
			val first = Math.min(p1Index0, p1Index1)
			val last = Math.max(p1Index0, p1Index1)
			// If the indices of the consumed polygon are consecutive then
			// the new vertices are the ones after the last line point followed by
			// the vertices before the first line point
			if (last - first == 1) {
				p0.addAll(p1.subList(last + 1, p1.size))
				p0.addAll(p1.subList(0, first))
			}
			// Otherwise the vertices of the line are the first and last of the polygon
			// and the new vertices are all the ones inbetween
			else {
				p0.addAll(p1.subList(1, last))
			}
			// And the final vertices to the merged polygon, delete the consumed polygon
			// and set the pointer to the consumed polygon to point to the new polygon
			p0.addAll(addAtEnd)
			p1.clear()
			polygonIndexes[polys.last()] = p0Index

			// Removed the dissolved line and set the current polygon to the
			// consuming polygon
			sharedLines.removeIf { it.key == line }
			currentPolygon = p0Index
		}

		return polygons.filter { it.size >= 3 }
	}

	override fun toString(): String {
		return "Polygon[$vertices]"
	}

	companion object {
		fun getAngle(polygon: List<Int>, vertexIndex: Int, vertices: List<Vec2>): Float {
			val polygonIndex = polygon.indexOf(vertexIndex)
			val previousIndex = (polygonIndex + polygon.size - 1) % polygon.size
			val nextIndex = (polygonIndex + 1) % polygon.size
			val vertex = vertices[vertexIndex]
			return (vertices[polygon[previousIndex]] - vertex).angleBetween(vertices[polygon[nextIndex]] - vertex)
		}

		/**
		 * Returns if removing the line between [polygon1] and [polygon2] would create a reflex angle at [vertexIndex]
		 */
		fun checkJoiningAngle(polygon1: List<Int>, polygon2: List<Int>, vertexIndex: Int, vertices: List<Vec2>): Boolean {
			val p0Angle = getAngle(polygon1, vertexIndex, vertices)
			val p1Angle = getAngle(polygon2, vertexIndex, vertices)
			// Removing this line would create a reflex angle, so it must be left
			return (p0Angle < 0f || p1Angle < 0f || p0Angle + p1Angle > PIF)
		}
	}
}