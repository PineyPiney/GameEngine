package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class ButtonComponent(
	parent: GameObject,
	val onClick: (button: ButtonComponent, cursorPos: Vec2) -> Unit,
	val onUnClick: (button: ButtonComponent, cursorPos: Vec2) -> Unit = { _, _ -> },
	val onEnter: (button: ButtonComponent, cursorPos: Vec2, cursorDelta: Vec2) -> Unit = { _, _, _ -> },
	val onExit: (button: ButtonComponent, cursorPos: Vec2, cursorDelta: Vec2) -> Unit = { _, _, _ -> }
) : DefaultInteractorComponent(parent) {

	var active: Boolean = true

	override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
		super.onInput(window, input, action, cursorPos)
		if (input == InputState(GLFW.GLFW_GAMEPAD_BUTTON_A, ControlType.GAMEPAD_BUTTON) && active) {
			when (action) {
				GLFW.GLFW_PRESS -> onClick(this, cursorPos)
				GLFW.GLFW_RELEASE -> onUnClick(this, cursorPos)
			}
		}
		return action
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if (active) {
			when (action) {
				GLFW.GLFW_PRESS -> onClick(this, cursorPos)
				GLFW.GLFW_RELEASE -> onUnClick(this, cursorPos)
			}
		}
		return action
	}

	override fun onCursorEnter(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		onEnter(this, cursorPos, cursorDelta)
	}

	override fun onCursorExit(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		onExit(this, cursorPos, cursorDelta)
	}
}