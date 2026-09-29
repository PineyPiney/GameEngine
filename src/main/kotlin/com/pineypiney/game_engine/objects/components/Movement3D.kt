package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.util.input.gamepads.*
import com.pineypiney.game_engine.util.input.knm.CursorPosition
import com.pineypiney.game_engine.util.input.knm.MouseInput
import com.pineypiney.game_engine.util.jna.LibScePad
import com.pineypiney.game_engine.util.maths.eulerToVector
import com.pineypiney.game_engine.util.maths.up
import com.pineypiney.game_engine.util.maths.vectorToEuler
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.f
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

class Movement3D(parent: GameObject, val camera: CameraI, val window: WindowI, var speed: Float, val boost: Float = 5f, val defaultYawPitch: Vec2d = Vec2d(180.0, 0.0)) :
	DefaultInteractorComponent(parent), PreRenderComponent {

	override val whenVisible: Boolean = false
	var move = true
	var look = true

	var yaw = defaultYawPitch.x
	var pitch = defaultYawPitch.y

	var gamepadID = -1

	override fun shouldInteract(): Boolean {
		return move || look
	}

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		val pad = window.input.getInputOrNull<GamepadInput>()?.getController(gamepadID)
		if(move) {
			val travel = Vec3()

			// Horizontal component of camera.front
			val forward = camera.cameraUp cross camera.cameraRight

			val x: Float
			val y: Float
			val z: Float
			val sprint: Float

			if (pad == null) {
				x = (window.getKey(GLFW.GLFW_KEY_D) - window.getKey(GLFW.GLFW_KEY_A)).toFloat()
				y = (window.getKey(GLFW.GLFW_KEY_SPACE) - window.getKey(GLFW.GLFW_KEY_LEFT_CONTROL)).toFloat()
				z = (window.getKey(GLFW.GLFW_KEY_W) - window.getKey(GLFW.GLFW_KEY_S)).toFloat()

				sprint = window.getKey(GLFW.GLFW_KEY_LEFT_SHIFT).toFloat()
			} else {
				x = pad.axesStates[GamePad.AXIS_LEFT_X]
				y = pad.getButton(GamePad.BUTTON_A).f - pad.getButton(GamePad.BUTTON_B).f
				z = pad.axesStates[GamePad.AXIS_LEFT_Y]

				sprint = pad.axesStates[GamePad.AXIS_RIGHT_TRIGGER]
			}

			travel += camera.cameraRight * x
			travel += camera.cameraUp * y
			travel += forward * z
			if (travel.length2() > 1f) travel.normalizeAssign()

			val feedbackValue = travel.length2() * sprint
			(pad as? RumbleGamepad)?.setRumble(feedbackValue * .4f, feedbackValue)
			(pad as? LightBarGamepad)?.setLightBar(Vec3(0f, 0f, feedbackValue))
			(pad as? TriggerEffectGamepad)?.run {
				setLeftTriggerEffect(LibScePad.TriggerEffectFeedback(2, 6))
				setRightTriggerEffect(LibScePad.TriggerEffectFeedback(2, 6))
			}

			travel *= (1f + boost * sprint)

			if (travel != Vec3(0)) {
				camera.translate(travel * speed * Timer.frameDelta)
			}
		}
		if (look && pad != null) {
			yaw += pad.rightJoystick.x * 100 * Timer.frameDelta
			pitch = (pitch + pad.rightJoystick.y * 100 * Timer.frameDelta).coerceIn(-89.99, 89.99)
			updateVectors()
		}
	}

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		if (look && gamepadID == -1) {
			window.input.getInput<MouseInput>().setCursorAt(Vec2(0))
			yaw += cursorDelta.position.x * 20
			pitch = (pitch + cursorDelta.position.y * 20).coerceIn(-89.99, 89.99)
			updateVectors()
		}
	}

	fun updateAngles() {
		val rotation = Quat(camera.cameraUp, up)
		val relativeForward = (rotation * camera.cameraFront).normalize()
		val (p, y) = vectorToEuler(relativeForward)
		pitch = Math.toDegrees(p.toDouble())
		yaw = Math.toDegrees(y.toDouble())
		camera.updateCameraRight()
	}

	fun updateVectors() {
		val rotation = Quat(up, camera.cameraUp)
		val relativeForward = eulerToVector(Math.toRadians(yaw), Math.toRadians(pitch))
		rotation.times(relativeForward, camera.cameraFront)
		camera.updateCameraRight()
	}

	fun resetLook() {
		yaw = defaultYawPitch.x
		pitch = defaultYawPitch.y
		updateVectors()
	}

	companion object {
		fun default(window: WindowI, camera: CameraI, speed: Float, boost: Float = 5f, defaultYawPitch: Vec2d = Vec2d(180.0, 0.0)) =
			Movement3D(GameObject("Movement 3D"), camera, window, speed, boost, defaultYawPitch).applied()
	}
}