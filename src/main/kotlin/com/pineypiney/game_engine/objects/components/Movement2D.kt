package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.gamepads.Gamepad
import com.pineypiney.game_engine.util.input.gamepads.GamepadInput
import com.pineypiney.game_engine.window.Window
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

open class Movement2D(parent: GameObject, val window: Window, var speed: Float = 4f, var sprintBoost: Float = 2f) : Component(parent) {

	var keyboardRatio: Float = 1f
	var gamepadID = -1

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
			move *= (1f + sprintBoost * window.getKey(GLFW.GLFW_KEY_LEFT_SHIFT))

		} else {
			move += pad.leftJoystick
			move *= (1f + sprintBoost * pad.axesStates[Gamepad.AXIS_RIGHT_TRIGGER])
		}

		return move * speed
	}

	companion object {
		fun create(name: String, window: Window, speed: Float = 4f, sprintBoost: Float = 2f): Movement2D {
			val obj = GameObject(name)
			val mov = Movement2D(obj, window, speed, sprintBoost)
			obj.components.add(mov)
			return mov
		}
	}
}