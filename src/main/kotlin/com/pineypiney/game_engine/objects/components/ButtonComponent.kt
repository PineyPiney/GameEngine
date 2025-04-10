package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class ButtonComponent(
	parent: GameObject,
	val onClick: (button: ButtonComponent, cursorPos: Vec2) -> Unit,
	val onUnClick: (button: ButtonComponent, cursorPos: Vec2) -> Unit = { _, _ -> },
	val onEnter: (button: ButtonComponent, cursorPos: CursorPosition, cursorDelta: CursorPosition) -> Unit = { _, _, _ -> },
	val onExit: (button: ButtonComponent, cursorPos: CursorPosition, cursorDelta: CursorPosition) -> Unit = { _, _, _ -> }
) : DefaultInteractorComponent(parent) {

	var active: Boolean = true

	override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: CursorPosition): Int {
		super.onInput(window, input, action, cursorPos)
		if (input == InputState(GLFW.GLFW_GAMEPAD_BUTTON_A, ControlType.GAMEPAD_BUTTON) && active) {
			when (action) {
				GLFW.GLFW_PRESS -> onClick(this, cursorPos.position)
				GLFW.GLFW_RELEASE -> onUnClick(this, cursorPos.position)
			}
		}
		return action
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if (active) {
			when (action) {
				GLFW.GLFW_PRESS -> onClick(this, cursorPos.position)
				GLFW.GLFW_RELEASE -> onUnClick(this, cursorPos.position)
			}
		}
		return action
	}

	override fun onCursorEnter(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		onEnter(this, cursorPos, cursorDelta)
	}

	override fun onCursorExit(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		onExit(this, cursorPos, cursorDelta)
	}
}