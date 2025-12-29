package com.pineypiney.game_engine.objects.components.widgets

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class ButtonComponent(
	parent: GameObject,
	val onClick: (button: ButtonComponent, cursorPos: Vec2) -> Unit,
	val onUnClick: (button: ButtonComponent, cursorPos: Vec2) -> Unit = { _, _ -> },
	val onEnter: (button: ButtonComponent, cursorPos: CursorPosition, cursorDelta: CursorPosition) -> Unit = { _, _, _ -> },
	val onExit: (button: ButtonComponent, cursorPos: CursorPosition, cursorDelta: CursorPosition) -> Unit = { _, _, _ -> }
) : DefaultInteractorComponent(parent) {

	var active: Boolean = true

	override fun onInput(window: WindowI, state: InputState, action: Int, cursorPos: CursorPosition): Int {
		super.onInput(window, state, action, cursorPos)
		if (state == InputState(GLFW.GLFW_GAMEPAD_BUTTON_A, ControlType.GAMEPAD_BUTTON) && active) {
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

	companion object {

		fun createSpriteButton(name: String,
							   sprite: Sprite,
							   shader: Shader = ColouredSpriteComponent.colouredMenuShader,
							   baseTint: Vec4 = Vec4(1f),
							   hoverTint: Vec4 = Vec4(.95f),
							   clickTint: Vec4 = Vec4(.9f),
							   action: (ButtonComponent, Vec2) -> Unit): GameObject{

			val obj = GameObject(name, 1)
			val getColour = { b: ButtonComponent ->
				when {
					b.pressed -> clickTint
					b.hover -> hoverTint
					else -> baseTint
				}
			}

			obj.components.addAll(
				ButtonComponent(obj, { b, v ->
					action(b, v)
					b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
				}, { b, _ ->
					b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
				}, { b, _, _ ->
					b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
				}, { b, _, _ ->
					b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
				}),
				ColouredSpriteComponent(obj, sprite, baseTint, shader)
			)
			return obj
		}
	}
}