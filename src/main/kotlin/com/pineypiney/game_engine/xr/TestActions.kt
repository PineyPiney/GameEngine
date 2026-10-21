package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.xr.actions.*
import org.lwjgl.openxr.XrAction
import org.lwjgl.openxr.XrActionSuggestedBinding
import org.lwjgl.openxr.XrInstance
import org.lwjgl.system.MemoryStack

class TestActions : OpenXrActionSet("test_action_set", "Test Action Set") {

	var leftHandPath: Long = 0L
	var rightHandPath: Long = 0L

	lateinit var grabAction: FloatAction
	lateinit var moveAction: Vec2Action
	lateinit var lookAction: Vec2Action
	lateinit var palmPoseAction: PoseAction
	lateinit var hapticAction: VibrationAction

	override fun addActions(stack: MemoryStack, instance: XrInstance) {
		leftHandPath = XrUtil.createPath(stack, instance, "/user/hand/left")
		rightHandPath = XrUtil.createPath(stack, instance, "/user/hand/right")

		grabAction = addAction(FloatAction("grab_cube", "Grab Cube", listOf(leftHandPath, rightHandPath)))
		moveAction = addAction(Vec2Action("move", "Move", listOf(leftHandPath)))
		lookAction = addAction(Vec2Action("look", "Look", listOf(rightHandPath)))
		palmPoseAction = addAction(PoseAction("palm_pose", "Palm Pose", listOf(leftHandPath, rightHandPath)))
		hapticAction = addAction(VibrationAction("haptic", "Haptic", listOf(leftHandPath, rightHandPath)))
	}

	override fun bindActions(stack: MemoryStack, instance: XrInstance): Map<String, XrActionSuggestedBinding.Buffer> {

		val khronosList = mutableListOf<Pair<XrAction, String>>()
		grabAction.addBindings(instance, khronosList, "/input/select/click")
		palmPoseAction.addBindings(instance, khronosList, "/input/grip/pose")
		hapticAction.addBindings(instance, khronosList, "/output/haptic")
		val suggestedKhronosBindings = XrUtil.createSuggestedBindings(stack, khronosList)

		val oculusList = mutableListOf<Pair<XrAction, String>>()
		grabAction.addBindings(instance, oculusList, "/input/squeeze/value")
		moveAction.addBindings(instance, oculusList, "/input/thumbstick")
		lookAction.addBindings(instance, oculusList, "/input/thumbstick")
		palmPoseAction.addBindings(instance, oculusList, "/input/grip/pose")
		hapticAction.addBindings(instance, oculusList, "/output/haptic")
		val suggestedOculusBindings = XrUtil.createSuggestedBindings(stack, oculusList)

		return mapOf(
			"/interaction_profiles/khr/simple_controller" to suggestedKhronosBindings,
			"/interaction_profiles/oculus/touch_controller" to suggestedOculusBindings,
		)
	}

	override fun bindToSession(stack: MemoryStack, session: OpenXrSession) {
		super.bindToSession(stack, session)
		palmPoseAction.baseSpace = session.space
	}
}