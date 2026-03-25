package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.maths.eulerToVector
import com.pineypiney.game_engine.util.maths.up
import com.pineypiney.game_engine.util.maths.vectorToEuler
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

class Movement3D(parent: GameObject, val camera: Camera, val window: WindowI, var speed: Float, val boost: Float = 5f) : DefaultInteractorComponent(parent), PreRenderComponent {

	override val whenVisible: Boolean = false
	var move = true
	var look = true

	var yaw = -90.0
	var pitch = 0.0

	override fun shouldInteract(): Boolean {
		return move || look
	}

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		if(move) {
			val travel = Vec3()

			val forward = camera.cameraUp cross camera.cameraRight
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_W) == 1) travel += forward
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_S) == 1) travel -= forward
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_A) == 1) travel -= camera.cameraRight
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_D) == 1) travel += camera.cameraRight
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_SPACE) == 1) travel += camera.cameraUp
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) == 1) travel -= camera.cameraUp
			if (GLFW.glfwGetKey(window.windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) == 1) travel *= boost

			if (travel != Vec3(0)) {
				camera.translate(travel * speed * Timer.frameDelta)
			}
		}
	}

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		if(look) {
			window.input.mouse.setCursorAt(Vec2(0))
			yaw += cursorDelta.position.x * 20
			pitch = (pitch + cursorDelta.position.y * 20).coerceIn(-89.99, 89.99)
			updateVectors()
		}
	}

	fun updateAngles() {
		val rotation = Quat(camera.cameraUp, up)
		val relativeForward = (rotation * camera.cameraFront).normalize()
		val angles = vectorToEuler(relativeForward)
		pitch = Math.toDegrees(angles.first.toDouble())
		yaw = Math.toDegrees(angles.second.toDouble())
		camera.updateCameraRight()
	}

	fun updateVectors() {
		val rotation = Quat(up, camera.cameraUp)
		val relativeForward = eulerToVector(Math.toRadians(yaw), Math.toRadians(pitch))
		rotation.times(relativeForward, camera.cameraFront)
		camera.updateCameraRight()
	}

	fun resetLook() {
		yaw = -90.0
		pitch = 0.0
		updateVectors()
	}

	companion object {
		fun default(window: WindowI, camera: Camera, speed: Float) = Movement3D(GameObject("Movement 3D"), camera, window, speed).applied()
	}
}