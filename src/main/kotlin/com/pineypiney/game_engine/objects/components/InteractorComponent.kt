package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.components.rendering.RenderedComponent
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

interface InteractorComponent : ComponentI {

	var hover: Boolean
	var pressed: Boolean
	var forceUpdate: Boolean
	var passThrough: Boolean

	fun checkHover(ray: Ray, cursor: CursorPosition): Float {
		val renderer = parent.getComponent<RenderedComponent>()

		if(renderer == null){
			return if(cursor.position.isWithin(
					Vec2(parent.transformComponent.worldPosition),
					Vec2(parent.transformComponent.worldScale)
				)) ray.rayOrigin.z - parent.transformComponent.worldPosition.z
			else -1f
		}

		val shape = renderer.getMeshShape()
		if(shape is Shape2D){
			val newShape = shape.transformedBy(parent.worldModel)
			return if(newShape.containsPoint(cursor.position)) ray.rayOrigin.z - parent.transformComponent.worldPosition.z
			else -1f
		}

		return if(cursor.position.isWithin(
			Vec2(parent.transformComponent.worldPosition) - (shape.min.run{ Vec2(x, y) } * Vec2(parent.transformComponent.worldScale)),
			Vec2(parent.transformComponent.worldScale) * shape.size.run { Vec2(x, y) }
		)) ray.rayOrigin.z - parent.transformComponent.worldPosition.z
		else -1f
	}

	fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		if (pressed) onDrag(window, cursorPos, cursorDelta, ray)
	}

	fun onCursorEnter(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {}
	fun onCursorExit(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {}
	fun onDrag(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {}
	fun onScroll(window: WindowI, scrollDelta: Vec2): Int = 0

	fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: CursorPosition): Int {
		return when {
			input.i == 0 && input.controlType == ControlType.MOUSE -> onPrimary(window, action, input.mods, cursorPos)
			input.i == 1 && input.controlType == ControlType.MOUSE -> onSecondary(window, action, input.mods, cursorPos)
			else -> 0
		}
	}

	fun onType(window: WindowI, char: Char): Int = 0

	// This is the default action when an item is clicked
	fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		// Continue pressing if already pressed, or start pressing on first click
		pressed = (pressed && action == GLFW.GLFW_REPEAT) || action == GLFW.GLFW_PRESS
		return action
	}

	fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int = action

	fun shouldInteract(): Boolean {
		return this.hover || this.pressed || this.forceUpdate
	}

	companion object {
		const val INTERRUPT = -1
	}
}