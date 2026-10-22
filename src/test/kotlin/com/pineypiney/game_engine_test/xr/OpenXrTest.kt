package com.pineypiney.game_engine_test.xr

import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.openxr.DefaultOpenXrRenderer
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.up
import com.pineypiney.game_engine.xr.OpenXrGameLogicI
import com.pineypiney.game_engine.xr.OpenXrSession
import com.pineypiney.game_engine.xr.XrStructs
import com.pineypiney.game_engine.xr.mat4
import glm_.i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.openxr.XR10
import org.lwjgl.openxr.XrHapticBaseHeader
import org.lwjgl.openxr.XrSpaceLocation
import org.lwjgl.system.MemoryStack

class OpenXrTest(override val gameEngine: TestOpenXrEngine) : GameLogic(), OpenXrGameLogicI {

	override val id: String = "OpenXR Test"
	override val renderer = DefaultOpenXrRenderer<OpenXrTest>(gameEngine.window, gameEngine.openxr.renderManager)

	val leftHand = GameObject.simpleRenderedGameObject("Left Hand", ColourRendererComponent.shader3D, Vec3(0f), Vec3(.1f), Mesh.centerCubeShape){
		uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 1f, 1f) }
	}
	val rightHand = GameObject.simpleRenderedGameObject("Left Hand", ColourRendererComponent.shader3D, Vec3(0f), Vec3(.1f), Mesh.centerCubeShape){
		uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 1f, 1f) }
	}

	override fun addObjects() {
		for(i in 0..99){
			add(GameObject.simpleRenderedGameObject("Colour Cube $i", ColourRendererComponent.shader3D, Vec3(-4.5f + (i % 10), -4.5f + (i / 10), 0f) * .15f, Vec3(.15f), Mesh.centerCubeShape){
				uniforms.setVec4Uniform("colour") { Vec4((i % 10) * .1f, (i / 10) * .1f, 0f, 1f) }
			})
		}
		add(leftHand, rightHand)
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun processActions(session: OpenXrSession, time: Long) {

		if(gameEngine.window.getKey(' '.code) == 1) gameEngine.window.shouldClose = true

		MemoryStack.stackPush().use { stack ->

			val location = XrSpaceLocation.calloc(stack).`type$Default`()
			XR10.xrLocateSpace(session.viewSpace, session.space, time, location)
			val headMat = location.pose().mat4()
			val rotation = headMat.getRotation()
			val moveState = gameEngine.actionSet.moveAction.getValue(session.leftHandPath).rotate(renderer.rotationOffset)
			val direction = rotation * Vec3(moveState.x, 0f, -moveState.y)

			val vertical = gameEngine.actionSet.upAction.getValue(session.rightHandPath).i - gameEngine.actionSet.downAction.getValue(session.rightHandPath).i
			direction.y += vertical

			renderer.viewOffset += (direction * Timer.frameDelta)

			val lookState = gameEngine.actionSet.lookAction.getValue(session.rightHandPath)
			renderer.rotationOffset -= (lookState * Timer.frameDelta.toFloat())

			val vibration = XrStructs.hapticVibration(stack, gameEngine.actionSet.grabAction.getValue(session.leftHandPath) * .5f)
			gameEngine.actionSet.hapticAction.setValue(session, session.leftHandPath, XrHapticBaseHeader.create(vibration))
			gameEngine.actionSet.hapticAction.setValue(session, session.rightHandPath, XrHapticBaseHeader.create(vibration.amplitude(gameEngine.actionSet.grabAction.getValue(session.rightHandPath) * .5f)))
		}

		val mat = I.translateAssign(renderer.viewOffset).rotateAssign(renderer.rotationOffset, up)
		val left = mat * gameEngine.actionSet.leftHand
		val right = mat * gameEngine.actionSet.rightHand
		leftHand.position = left.getTranslation()
		leftHand.rotation = left.getRotation()
		rightHand.position = right.getTranslation()
		rightHand.rotation = right.getRotation()
	}
}