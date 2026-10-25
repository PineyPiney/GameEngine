package com.pineypiney.game_engine_test.xr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.ModelRendererComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.openxr.DefaultOpenXrRenderer
import com.pineypiney.game_engine.resources.models.GLTFModelLoader
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.util.LwjglUtil
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.hasAll
import com.pineypiney.game_engine.util.extension_functions.rotate
import com.pineypiney.game_engine.util.maths.up
import com.pineypiney.game_engine.xr.*
import glm_.glm
import glm_.i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack
import java.io.ByteArrayInputStream
import kotlin.concurrent.thread

class OpenXrTest(override val gameEngine: TestOpenXrEngine) : GameLogic(), OpenXrGameLogicI {

	override val id: String = "OpenXR Test"
	override val renderer = DefaultOpenXrRenderer<OpenXrTest>(gameEngine.window, gameEngine.openxr.renderManager)

	lateinit var updateHands: () -> Unit

	val viewOffset = Vec3(0f, 0f, -2f)
	var rotationOffset = 0f

	var lastLoadAttempt = 0.0
	private var loadedModels = false
	val controllerModels = mutableMapOf<ResourceKey, Model>()

	override fun addObjects() {
		if(TestOpenXrEngine.supportsHandTracking){
			val handCuboids = if(TestOpenXrEngine.supportsHandTracking) EXTHandTracking.XR_HAND_JOINT_COUNT_EXT else 1

			if(FBHandTrackingMesh.XR_FB_HAND_TRACKING_MESH_EXTENSION_NAME in OpenXrManager.enabledExtensions){
				val leftHandMesh = gameEngine.hands[0].getMesh(gameEngine.resourcesLoader.factory)
				val rightHandMesh = gameEngine.hands[1].getMesh(gameEngine.resourcesLoader.factory)

				val leftHand = GameObject.simpleRenderedGameObject("Left Hand Mesh", ModelRendererComponent.modelUnlit, Vec3(0), Vec3(1f), leftHandMesh) {
					uniforms.setMat4sUniform("boneTransforms[0]", gameEngine.hands[0]::getBoneTransforms)
					uniforms.setFloatUniform("material.alpha"){ 1f }
				}

				val rightHand = GameObject.simpleRenderedGameObject("Right Hand Mesh", ModelRendererComponent.modelUnlit, Vec3(0), Vec3(1f), rightHandMesh) {
					uniforms.setMat4sUniform("boneTransforms[0]", gameEngine.hands[1]::getBoneTransforms)
					uniforms.setFloatUniform("material.alpha"){ 1f }
				}
				add(leftHand, rightHand)

				updateHands = {
					gameEngine.hands[0].updateJoints()
					gameEngine.hands[1].updateJoints()
				}
			}
			else {
				val leftHand = Array(handCuboids) {
					GameObject.simpleRenderedGameObject("Left Hand $it", ColourRendererComponent.shader3D, Vec3(0f), Vec3(.1f), Mesh.centerCubeShape) {
						uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 1f, 1f) }
					}
				}
				val rightHand = Array(handCuboids) {
					GameObject.simpleRenderedGameObject("Right Hand $it", ColourRendererComponent.shader3D, Vec3(0f), Vec3(.1f), Mesh.centerCubeShape) {
						uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 1f, 1f) }
					}
				}
				add(*leftHand, *rightHand)

				updateHands = {
					repeat(handCuboids){ i ->
						val scale = Vec3(1.5f, 1.5f, 2.5f)
						val leftJoint = gameEngine.hands[0].joints[i]
						if(leftJoint.locationFlags() hasAll HandTracker.ALL_FLAGS){
							val left = leftJoint.pose().mat4()
							leftHand[i].position = left.getTranslation()
							leftHand[i].rotation = left.getRotation()
							leftHand[i].scale = scale * leftJoint.radius()
						}
						val rightJoint = gameEngine.hands[1].joints[i]
						if(rightJoint.locationFlags() hasAll HandTracker.ALL_FLAGS) {
							val right = rightJoint.pose().mat4()
							rightHand[i].position = right.getTranslation()
							rightHand[i].rotation = right.getRotation()
							rightHand[i].scale = scale * rightJoint.radius()
						}
					}
				}
			}

		}
		else {
			val leftHand = GameObject.simpleRenderedGameObject("Left Hand Cube", ColourRendererComponent.shader3D, Vec3(0f), Vec3(.1f), Mesh.centerCubeShape) {
				uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 1f, 1f) }
			}

			val rightHand = GameObject.simpleRenderedGameObject("Right Hand Cube", ColourRendererComponent.shader3D, Vec3(0f), Vec3(.1f), Mesh.centerCubeShape) {
				uniforms.setVec4Uniform("colour") { Vec4(0f, 0f, 1f, 1f) }
			}

			add(leftHand, rightHand)

			updateHands = {
				val left = gameEngine.actionSet.leftHand
				val right = gameEngine.actionSet.rightHand
				leftHand.position = left.getTranslation()
				leftHand.rotation = left.getRotation()
				rightHand.position = right.getTranslation()
				rightHand.rotation = right.getRotation()
			}
		}
		for(i in 0..99){
			add(GameObject.simpleRenderedGameObject("Colour Cube $i", ColourRendererComponent.shader3D, Vec3(-4.5f + (i % 10), -4.5f + (i / 10), 0f) * .15f, Vec3(.15f), Mesh.centerCubeShape){
				uniforms.setVec4Uniform("colour") { Vec4((i % 10) * .1f, (i / 10) * .1f, 0f, 1f) }
			})
		}
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun processActions(session: OpenXrSession, time: Long) {

		if(gameEngine.window.getKey(' '.code) == 1) session.exit()
		if(gameEngine.window.getKey('L'.code) == 1 && Timer.time - lastLoadAttempt > 2.0) loadControllerModels()

		MemoryStack.stackPush().use { stack ->
			val lookState = gameEngine.actionSet.lookAction.getValue(session.rightHandPath)
			rotationOffset += (lookState * Timer.frameDelta.toFloat())

			val location = XrSpaceLocation.calloc(stack).`type$Default`()
			XR10.xrLocateSpace(session.viewSpace, session.space, time, location)
			val headMat = location.pose().mat4()
			val rotation = headMat.getRotation()
			val moveState = gameEngine.actionSet.moveAction.getValue(session.leftHandPath)
			val direction = rotation * Vec3(-moveState.x, 0f, moveState.y)

			val vertical = gameEngine.actionSet.downAction.getValue(session.rightHandPath).i - gameEngine.actionSet.upAction.getValue(session.rightHandPath).i
			direction.y += vertical
			viewOffset += (direction * Timer.frameDelta)

			session.setSpaceOffset(viewOffset.rotate(Vec3(0f, rotationOffset, 0f)), glm.angleAxis(rotationOffset, up))

			val vibration = XrStructs.hapticVibration(stack, gameEngine.actionSet.grabAction.getValue(session.leftHandPath) * .5f)
			gameEngine.actionSet.hapticAction.setValue(session, session.leftHandPath, XrHapticBaseHeader.create(vibration))
			gameEngine.actionSet.hapticAction.setValue(session, session.rightHandPath, XrHapticBaseHeader.create(vibration.amplitude(gameEngine.actionSet.grabAction.getValue(session.rightHandPath) * .5f)))

			if(TestOpenXrEngine.supportsHandTracking) {
				for(hand in gameEngine.hands){
					hand.poll(stack, session, time)
				}
			}
			updateHands()
		}
	}

	// This is throwing XR_ERROR_RUNTIME_FAILURE on xrLoadRenderModelFB, not sure why
	fun loadControllerModels(){
		lastLoadAttempt = Timer.time
		if(FBRenderModel.XR_FB_RENDER_MODEL_EXTENSION_NAME in OpenXrManager.enabledExtensions){
			thread {
				MemoryStack.stackPush().use { stack ->
					val paths = LwjglUtil.enumerateStructs(stack, "FB Render Model Paths", gameEngine.openxr.session.session, XrRenderModelPathInfoFB::`type$Default`, XrRenderModelPathInfoFB::calloc, FBRenderModel::xrEnumerateRenderModelPathsFB) ?: return@use
					val properties = XrRenderModelPropertiesFB.calloc(stack).`type$Default`()
					val loadInfo = XrRenderModelLoadInfoFB.calloc(stack).`type$Default`()
					val modelBuffer = XrRenderModelBufferFB.calloc(stack).`type$Default`()
					for(path in paths) {
						val pathString = XrUtil.readPath(stack, gameEngine.openxr.instance, path.path())
						if(!XrUtil.processResult(FBRenderModel.xrGetRenderModelPropertiesFB(gameEngine.openxr.session.session, path.path(), properties), "Failed to get FB Render Model Properties for model $pathString")) continue
						GameEngineI.logger.info("Found FB Render Model ${properties.modelNameString()} version ${properties.modelVersion()} key ${properties.modelKey()}")
						if(properties.modelKey() == FBRenderModel.XR_NULL_RENDER_MODEL_KEY_FB) continue
						try {
							XrUtil.processResult(FBRenderModel.xrLoadRenderModelFB(gameEngine.openxr.session.session, loadInfo.modelKey(properties.modelKey()), modelBuffer.bufferCapacityInput(0)), "Failed to load FB Render Model ${properties.modelNameString()}")
							val buffer = stack.malloc(modelBuffer.bufferCountOutput())
							XrUtil.processResult(FBRenderModel.xrLoadRenderModelFB(gameEngine.openxr.session.session, loadInfo, modelBuffer.bufferCapacityInput(modelBuffer.bufferCountOutput()).buffer(buffer)), "Failed to load FB Render Model ${properties.modelNameString()}")
							val bytes = ByteArray(buffer.capacity())
							buffer.get(bytes, 0, bytes.size)
							val stream = ByteArrayInputStream(bytes)
							GLTFModelLoader().loadGLBFile(gameEngine.resourcesLoader.factory, properties.modelNameString(), stream, controllerModels)
						}
						catch(e: AssertionError) {
							GameEngineI.logger.error(e.message)
						}
					}
					loadedModels = true
				}
			}
		}
	}

	companion object {
//		val upRotation = glm.angleAxi
	}
}