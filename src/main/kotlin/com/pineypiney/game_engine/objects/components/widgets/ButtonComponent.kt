package com.pineypiney.game_engine.objects.components.widgets

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.util.text.Text
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class ButtonComponent(
	parent: GameObject,
	val onClick: ButtonAction,
	val onUnClick: ButtonAction = { _, _ -> },
	val onEnter: ButtonHover = { _, _, _ -> },
	val onExit: ButtonHover = { _, _, _ -> }
) : DefaultInteractorComponent(parent) {

	var active: Boolean = true

	override fun onInput(window: WindowI, state: InputState, action: Int, cursorPos: CursorPosition): Int {
		if (super.onInput(window, state, action, cursorPos) == INTERRUPT) return INTERRUPT
		if (state == InputState(GLFW.GLFW_GAMEPAD_BUTTON_A, ControlType.GAMEPAD_BUTTON) && active) {
			when (action) {
				GLFW.GLFW_PRESS -> onClick(this, cursorPos.position)
				GLFW.GLFW_RELEASE -> onUnClick(this, cursorPos.position)
			}
			return INTERRUPT
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
			return INTERRUPT
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

		fun createTextButton(text: String, origin: Vec2, size: Vec2, action: ButtonAction): ButtonComponent {
			val obj = GameObject("$text Button", 1)
			obj.os(origin, size)
			return addTextButton(obj, text, Vec4(0, 0, 0, 1), action)
		}

		fun createTextButton(text: String, origin: Vec2, size: Vec2, action: (ButtonComponent) -> Unit): ButtonComponent {
			val obj = GameObject("$text Button", 1)
			obj.os(origin, size)
			return addTextButton(obj, text, Vec4(0, 0, 0, 1), { b, _ -> action(b) })
		}

		fun createTextButton(text: String, pos: Vec2i, size: Vec2i, origin: Vec2, action: (ButtonComponent) -> Unit): ButtonComponent {
			val obj = GameObject("$text Button", 1)
			obj.pixel(pos, size, origin)
			return addTextButton(obj, text, Vec4(0, 0, 0, 1), { b, _ -> action(b) })
		}

		fun addTextButton(obj: GameObject, text: String, textColour: Vec4, action: ButtonAction): ButtonComponent {

			val baseColour = Vec4.fromHex(0x00BFFF)
			val hoverColour = Vec4.fromHex(0x008CFF)
			val clickColour = Vec4.fromHex(0x026FFF)

			val selectColour = { but: ButtonComponent ->
				when {
					but.pressed -> clickColour
					but.hover -> hoverColour
					else -> baseColour
				}
			}

			val setColour = { button: ButtonComponent ->
				button.parent.getComponent<ColourRendererComponent>()?.colour = selectColour(button)
			}

			val button = ButtonComponent(obj, { b, c -> action(b, c); setColour(b) }, { b, _ -> setColour(b) }, { b, _, _ -> setColour(b) }, { b, _, _ -> setColour(b) })
			obj.components.addAll(
				button,
				ColourRendererComponent(
					obj,
					baseColour,
					ColourRendererComponent.menuShader,
					Mesh.cornerSquareShape
				)
			)

			val textObject = Text.makeMenuText(text, textColour, 0, Text.ALIGN_CENTER)
			textObject.position = Vec3(0f, 0f, .001f)
			obj.addChild(textObject)

			return button
		}

		fun createSpriteButton(name: String,
							   sprite: Sprite,
							   origin: Vec3 = Vec3(0f),
							   size: Vec2 = Vec2(1f),
							   shader: RenderShader = ColouredSpriteComponent.colouredMenuShader,
							   baseTint: Vec4 = Vec4(1f),
							   hoverTint: Vec4 = Vec4(.95f),
							   clickTint: Vec4 = Vec4(.9f),
							   action: (ButtonComponent, Vec2) -> Unit): GameObject{

			val obj = GameObject(name, 1)
			obj.os(origin, size)
			val getColour = { b: ButtonComponent ->
				when {
					b.pressed -> clickTint
					b.hover -> hoverTint
					else -> baseTint
				}
			}
			addSpriteButton(obj, sprite, baseTint, shader, getColour, action)

			return obj
		}

		fun createSpriteButton(
			name: String,
			sprite: Sprite,
			pos: Vec2i,
			size: Vec2i,
			origin: Vec2 = Vec2(-1f),
			shader: RenderShader = ColouredSpriteComponent.colouredMenuShader,
			baseTint: Vec4 = Vec4(1f),
			hoverTint: Vec4 = Vec4(.95f),
			clickTint: Vec4 = Vec4(.9f),
			action: (ButtonComponent, Vec2) -> Unit
		): GameObject {

			val obj = GameObject(name, 1)
			obj.pixel(pos, size, origin)
			val getColour = { b: ButtonComponent ->
				when {
					b.pressed -> clickTint
					b.hover -> hoverTint
					else -> baseTint
				}
			}
			addSpriteButton(obj, sprite, baseTint, shader, getColour, action)

			return obj
		}

		fun addSpriteButton(obj: GameObject, sprite: Sprite, baseTint: Vec4, shader: RenderShader, getColour: (ButtonComponent) -> Vec4, action: ButtonAction): ButtonComponent {

			val button = ButtonComponent(obj, { b, v ->
				action(b, v)
				b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
			}, { b, _ ->
				b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
			}, { b, _, _ ->
				b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
			}, { b, _, _ ->
				b.parent.getComponent<ColouredSpriteComponent>()?.tint = getColour(b)
			})
			obj.components.addAll(
				button,
				ColouredSpriteComponent(obj, sprite, baseTint, shader)
			)
			return button
		}

		fun addButton(obj: GameObject, action: ButtonAction) {

		}
	}
}

typealias ButtonAction = (button: ButtonComponent, cursorPo: Vec2) -> Unit
typealias ButtonHover = (button: ButtonComponent, cursorPos: CursorPosition, cursorDelta: CursorPosition) -> Unit