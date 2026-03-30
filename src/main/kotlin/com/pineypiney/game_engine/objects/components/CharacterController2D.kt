package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW
import kotlin.math.abs

open class CharacterController2D(parent: GameObject, val window: WindowI, var speed: Float = 4f, var sprintBoost: Float = 2f) : Component(parent), UpdatingComponent {

	var keyboardRatio: Float = 1f
	var gamepadID = -1

	override fun update(interval: Float) {
		val move = getMovement()
		parent.getComponent<Rigidbody2DComponent>()?.velocity = move * speed
	}

	fun getMovement(): Vec2 {
		val move = Vec2()
		val pad = window.input.gamepad.getController(gamepadID)

		if (pad == null) {
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_W) == 1) move += Vec2(0, 1)
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_S) == 1) move += Vec2(0, -1)

			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_D) == 1) move += Vec2(1, 0)
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_A) == 1) move += Vec2(-1, 0)

			if (move.length2() > 1f) {
				move.x *= keyboardRatio
				move.normalizeAssign()
			}
			move *= (1f + sprintBoost * GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT))

		} else {
			val x = pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_X]
			if (abs(x) > pad.deadzone.x) move += Vec2(x, 0)
			val y = pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y]
			if (abs(y) > pad.deadzone.y) move += Vec2(0, -y)
			move *= (1f + sprintBoost * (.5f * (1f + pad.axesStates[GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER])))
		}

		return move
	}
}