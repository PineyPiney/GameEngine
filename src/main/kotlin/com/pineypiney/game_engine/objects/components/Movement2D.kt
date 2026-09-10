package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.input.gamepads.GamepadInput
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

open class Movement2D(parent: GameObject, val window: WindowI, var speed: Float = 4f, var sprintBoost: Float = 2f, val set: (Vec2) -> Unit) : Component(parent), PreRenderComponent {

	var keyboardRatio: Float = 1f
	var gamepadID = -1
	override val whenVisible: Boolean get() = false

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		set(getMovement() * speed * Timer.frameDelta)
	}

	fun getMovement(): Vec2 {
		val move = Vec2()
		val pad = window.input.getInputOrNull<GamepadInput>()?.getController(gamepadID)

		if (pad == null) {
			if (window.getKey(GLFW.GLFW_KEY_W) == 1) move += Vec2(0, 1)
			if (window.getKey(GLFW.GLFW_KEY_S) == 1) move += Vec2(0, -1)

			if (window.getKey(GLFW.GLFW_KEY_D) == 1) move += Vec2(1, 0)
			if (window.getKey(GLFW.GLFW_KEY_A) == 1) move += Vec2(-1, 0)

			if (move.length2() > 1f) {
				move.x *= keyboardRatio
				move.normalizeAssign()
			}
			move *= (1f + sprintBoost * GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT))

		} else {
			move += pad.leftJoystick
			move *= (1f + sprintBoost * (.5f * (1f + pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER])))
		}

		return move
	}
}