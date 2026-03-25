package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionPolygonRenderer
import com.pineypiney.game_engine.util.Cursor
import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.*
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

class ColliderEditor(parent: GameObject, screen: EditorScreen) : Transformer(parent, screen) {

	var grabPoint = Vec2(-1f)    // Cursor Screen Pos relative to the screen space of the point it is grabbing

	var hovered = -1
	var hoveredLine = -1
	var vector = Vec2()
	var secondPointOffset = Vec2()

	var lastClick = -1.0

	val rotateDist2 = .08f

	val moveCursor = Cursor(GLFW.GLFW_RESIZE_ALL_CURSOR)
	val resizeCursors = listOf(
		Cursor(GLFW.GLFW_RESIZE_NS_CURSOR),
		Cursor(GLFW.GLFW_RESIZE_NESW_CURSOR),
		Cursor(GLFW.GLFW_RESIZE_EW_CURSOR),
		Cursor(GLFW.GLFW_RESIZE_NWSE_CURSOR),
	)
	val rotateCursors = listOfNotNull(
		Cursor.create(screen.gameEngine, "textures/editor/rotate_ne.png", Vec2i(0, 32)),
		Cursor.create(screen.gameEngine, "textures/editor/rotate_se.png", Vec2i(0)),
		Cursor.create(screen.gameEngine, "textures/editor/rotate_sw.png", Vec2i(32, 0)),
		Cursor.create(screen.gameEngine, "textures/editor/rotate_nw.png", Vec2i(32)),
	)

	override var forceUpdate: Boolean = true

	override fun startAt(obj: GameObject, screen: EditorScreen) {
		super.startAt(obj, screen)
		val current = parent.getComponent<CollisionPolygonRenderer>()
		if (current?.obj != obj) {
			parent.removeComponent<CollisionPolygonRenderer>()
			parent.components.add(CollisionPolygonRenderer(parent, obj))
		}
	}

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if (pressed) return

		val obj = screen.editingObject ?: return
		val shape = obj.getComponent<Collider2DComponent>()?.transformedShape ?: return
		val points = getPoints(shape) ?: return
		val cursorWorldSpace = Vec2(getWorldPos(cursorPos.screenSpace))

		for ((i, p) in points.withIndex()) {
			val screenPos = getScreenPos(p)
			if ((screenPos - cursorPos.screenSpace).length2() < .0025f) {
				hovered = i
				hoveredLine = -1
				setResizeCursor(window, p - Vec2(obj.position))
				return
			}
		}

		vector = Vec2(Float.MAX_VALUE)
		for (i in points.indices) {
			val point1 = points[i]
			val point2 = points[(i + 1) % points.size]
			val screenPos1 = getScreenPos(point1)
			val screenPos2 = getScreenPos(point2)
			val line = Line2D(screenPos1, screenPos2)
			val vec = line.vectorTo(cursorPos.screenSpace)
			val dist2 = vec.length2()
			if (dist2 < .0025f) {
				hovered = -1
				hoveredLine = i
				val inv = obj.worldModel.inverse()
				secondPointOffset = point2.transformedBy(inv) - point1.transformedBy(inv)
				vector = vec
				setResizeCursor(window, -line.grad.normal())
				return
			} else if (shape.containsPoint(cursorWorldSpace)) vector = Vec2(0f)
			else if (dist2 < vector.length2()) vector = vec
		}

		hovered = -1
		hoveredLine = -1

		if (vector.length2() == 0f) {
			window.setCursor(moveCursor)
		} else if (vector.length2() < rotateDist2) {
			val angle = ((cursorWorldSpace - Vec2(obj.position)).angle() * 2f / PIF).toInt() % 4
			window.setCursor(rotateCursors[angle])
		} else window.setCursor(screen.cursor)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)

		val clickDelta: Double
		if (action == 1) {
			clickDelta = Timer.frameTime - lastClick
			lastClick = Timer.frameTime
		} else clickDelta = 0.0

		val obj = screen.editingObject ?: return action
		val collider = obj.getComponent<Collider2DComponent>() ?: return action
		val transformedShape = collider.transformedShape
		val points = getPoints(transformedShape) ?: return action

		if (hovered > -1) {
			val point = points[hovered]
			when (action) {
				1 -> {
					grabPoint = cursorPos.screenSpace - getScreenPos(point)
				}
//				0 -> if (oldPos.x != Float.MAX_VALUE) screen.editManager.addEdit(ComponentFieldEdit.moveEdit(
//					obj, screen, oldPos, obj.position
//				))
			}
			return INTERRUPT
		} else if (hoveredLine > -1) {
			val point = points[hoveredLine]
			when (action) {
				1 -> {
					if (clickDelta < .5 && transformedShape is Polygon) {
						val basePoints = (collider.shape as Polygon).vertices
						val points = basePoints.subList(0, hoveredLine + 1).toMutableList()
						val newPoint = Vec2(getWorldPos(cursorPos.screenSpace).transformedBy(obj.worldModel.inverse()))
						points.add(newPoint)
						points.addAll(basePoints.subList(hoveredLine + 1, basePoints.size))
						updateShape(obj, Polygon(points))
						grabPoint = Vec2(0f)
						hovered = hoveredLine + 1
						hoveredLine = -1
					} else {
						grabPoint = cursorPos.screenSpace - getScreenPos(point)
					}
				}
//				0 -> if (oldPos.x != Float.MAX_VALUE) screen.editManager.addEdit(ComponentFieldEdit.moveEdit(
//					obj, screen, oldPos, obj.position
//				))
			}
			return INTERRUPT
		} else {
			val dist2 = vector.length2()
			if (dist2 < rotateDist2) {
				when (action) {
					1 -> {
						grabPoint = cursorPos.screenSpace
					}
//					0 -> if (oldPos.x != Float.MAX_VALUE) screen.editManager.addEdit(ComponentFieldEdit.moveEdit(
//						obj, screen, oldPos, obj.position
//					))
				}
				return INTERRUPT
			}
		}

		return action
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onSecondary(window, action, mods, cursorPos)

		val obj = screen.editingObject ?: return action
		val shape = obj.getComponent<Collider2DComponent>()?.shape ?: return action

		if (hovered > -1) {
			when (action) {
				1 -> {
					// Delete Polygon vertices when right-clicking
					if (shape is Polygon) {
						val points = shape.vertices.toMutableList()
						points.removeAt(hovered)
						updateShape(obj, Polygon(points))
						hovered = -1
					}
				}
			}
			return INTERRUPT
		}

		return action
	}

	override fun onDrag(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {

		val obj = screen.editingObject ?: return
		val collider = obj.getComponent<Collider2DComponent>() ?: return
		val shape = collider.shape

		// Edit the collider of the editing object

		val newShape: Shape2D? = if (hovered > -1) {
			val dragPoint = cursorPos.screenSpace - grabPoint
			val dragWorldPoint = Vec2((getWorldPos(dragPoint)).transformedBy(obj.worldModel.inverse()))
			dragPoint(shape, dragWorldPoint)
		} else if (hoveredLine > -1) {
			val dragPoint = cursorPos.screenSpace - grabPoint
			val dragWorldPoint = Vec2((getWorldPos(dragPoint)).transformedBy(obj.worldModel.inverse()))
			dragLine(shape, dragWorldPoint)
		} else {
			val dist2 = vector.length2()
			if (dist2 == 0f) {
				val inv = obj.worldModel.inverse()
				val grabWorldPos = getWorldPos(grabPoint).transformedBy(inv)
				val cursorWorldPos = getWorldPos(cursorPos.screenSpace).transformedBy(inv)
				grabPoint = cursorPos.screenSpace

				shape.transformedBy(I.translate(cursorWorldPos - grabWorldPos))
			} else if (dist2 < rotateDist2) {
				val screenSpace = getScreenPos(Vec2(obj.position))
				val angle = (cursorPos.screenSpace - screenSpace).angleBetween(grabPoint - screenSpace)

				val shape = rotateShape(shape, angle)
				if (shape != null) grabPoint = cursorPos.screenSpace
				shape
			} else null
		}
		if (newShape != null) {
			updateShape(obj, newShape)
		}
	}

	fun dragPoint(shape: Shape2D, dragWorldPoint: Vec2): Shape2D? {
		return when (shape) {
			is Polygon -> {
				val points = shape.vertices.toMutableList()
				points[hovered] = dragWorldPoint
				Polygon(points)
			}

			is Rect2D -> {
				when (hovered) {
					0 -> {
						val distance = (shape.origin + shape.side1 + shape.side2) - dragWorldPoint
						val l1 = distance dot shape.side1.normalize()
						val l2 = distance dot shape.side2.normalize()
						Rect2D(dragWorldPoint, l1, l2, shape.angle)
					}

					1 -> {
						val distance = (shape.origin + shape.side1) - dragWorldPoint
						val l1 = distance dot shape.side1.normalize()
						val l2 = -(distance dot shape.side2.normalize())
						val point = dragWorldPoint - (shape.side2.normalize() * l2)
						Rect2D(point, l1, l2, shape.angle)
					}

					2 -> {
						val distance = dragWorldPoint - shape.origin
						val l1 = distance dot shape.side1.normalize()
						val l2 = distance dot shape.side2.normalize()
						Rect2D(shape.origin, l1, l2, shape.angle)
					}

					3 -> {
						val distance = (shape.origin + shape.side2) - dragWorldPoint
						val l1 = -(distance dot shape.side1.normalize())
						val l2 = distance dot shape.side2.normalize()
						val point = dragWorldPoint - (shape.side1.normalize() * l1)
						Rect2D(point, l1, l2, shape.angle)
					}

					else -> null
				}
			}

			else -> null
		}
	}

	fun dragLine(shape: Shape2D, dragWorldPoint: Vec2): Shape2D? {
		return when (shape) {
			is Polygon -> {
				val points = shape.vertices.toMutableList()
				points[hoveredLine] = dragWorldPoint
				points[(hoveredLine + 1) % points.size] = dragWorldPoint + secondPointOffset
				Polygon(points)
			}

			is Rect2D -> {
				when (hoveredLine) {
					0 -> {
						val distance = (shape.origin + shape.side1) - dragWorldPoint
						val l1 = distance dot shape.side1.normalize()
						val point = shape.origin + shape.side1 - (shape.side1.normalize() * l1)
						Rect2D(point, l1, shape.length2, shape.angle)
					}

					1 -> {
						val distance = dragWorldPoint - shape.origin
						val l2 = distance dot shape.side2.normalize()
						Rect2D(shape.origin, shape.length1, l2, shape.angle)
					}

					2 -> {
						val distance = dragWorldPoint - shape.origin
						val l1 = distance dot shape.side1.normalize()
						Rect2D(shape.origin, l1, shape.length2, shape.angle)
					}

					3 -> {
						val distance = (shape.origin + shape.side2) - dragWorldPoint
						val l2 = distance dot shape.side2.normalize()
						val point = shape.origin + shape.side2 - (shape.side2.normalize() * l2)
						Rect2D(point, shape.length1, l2, shape.angle)
					}

					else -> null
				}
			}

			else -> null
		}
	}

	fun dragShape(shape: Shape2D, movement: Vec2): Shape2D? {
		return when (shape) {
			is Polygon -> {
				val points = shape.vertices.toMutableList()
				for (point in points) {
					point += movement
				}
				Polygon(points)
			}

			else -> null
		}
	}

	fun rotateShape(shape: Shape2D, angle: Float): Shape2D? {
		val ctrl = screen.input.getMod(GLFW.GLFW_MOD_CONTROL)
		return when (shape) {
			is Rect2D -> {
				val rotation = if (ctrl) (((shape.angle + angle) * 8f / PIF).roundToInt() * PIF / 8f) - shape.angle else angle
				if (rotation == 0f) null
				else shape.transformedBy(I.rotate(rotation))
			}

			else -> {
				val rotation = if (ctrl) (angle * 8f / PIF).roundToInt() * PIF / 8f else angle
				if (rotation == 0f) null
				else shape.transformedBy(I.rotate(rotation))
			}
		}
	}

	fun updateShape(obj: GameObject, newShape: Shape2D) {
		obj.getComponent<Collider2DComponent>()?.shape = newShape
		screen.componentBrowser.refreshField("Collider2DComponent.shape")
	}

	fun getPoints(shape: Shape2D): List<Vec2>? {
		return when (shape) {
			is Polygon -> shape.vertices
			is Rect2D -> shape.points.toList()
			is Parallelogram -> shape.points.toList()
			else -> null
		}
	}

	fun getScreenPos(point: Vec2): Vec2 {
		return screen.renderer.camera.worldToScreen(Vec3(point))
	}

	fun getWorldPos(point: Vec2): Vec3 {
		return screen.renderer.camera.screenToWorld(point)
	}

	fun setResizeCursor(window: WindowI, vec: Vec2) {
		val angle = (vec.angle() * 4f / PIF).roundToInt() % 4
		window.setCursor(resizeCursors[angle])
	}

	override fun delete() {
		super.delete()
		moveCursor.delete()
		resizeCursors.delete()
		rotateCursors.delete()
		screen.window.setCursor(screen.cursor)
	}
}