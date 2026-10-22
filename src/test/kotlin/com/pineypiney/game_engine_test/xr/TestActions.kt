package com.pineypiney.game_engine_test.xr

import com.pineypiney.game_engine.xr.OpenXrSession
import com.pineypiney.game_engine.xr.XrUtil
import com.pineypiney.game_engine.xr.actions.*
import org.lwjgl.openxr.XrInstance
import org.lwjgl.system.MemoryStack

class TestActions : OpenXrActionSet("test_action_set", "Test Action Set") {

	var leftHandPath: Long = 0L
	var rightHandPath: Long = 0L

	lateinit var grabAction: FloatAction
	lateinit var moveAction: Vec2Action
	lateinit var lookAction: FloatAction
	lateinit var upAction: BoolAction
	lateinit var downAction: BoolAction

	lateinit var palmPoseAction: PoseAction
	lateinit var hapticAction: VibrationAction

	val leftHand get() = palmPoseAction.getValue(leftHandPath)
	val rightHand get() = palmPoseAction.getValue(rightHandPath)

	override fun addActions(stack: MemoryStack, instance: XrInstance) {
		leftHandPath = XrUtil.createPath(stack, instance, "/user/hand/left")
		rightHandPath = XrUtil.createPath(stack, instance, "/user/hand/right")

		grabAction = addAction(FloatAction("grab_cube", "Grab Cube", listOf(leftHandPath, rightHandPath)))
		moveAction = addAction(Vec2Action("move", "Move", listOf(leftHandPath)))
		lookAction = addAction(FloatAction("look", "Look", listOf(rightHandPath)))
		upAction = addAction(BoolAction("up", "Up", listOf(rightHandPath)))
		downAction = addAction(BoolAction("down", "Down", listOf(rightHandPath)))

		palmPoseAction = addAction(PoseAction("palm_pose", "Palm Pose", listOf(leftHandPath, rightHandPath)))
		hapticAction = addAction(VibrationAction("haptic", "Haptic", listOf(leftHandPath, rightHandPath)))
	}

	override fun bindActions(bindings: ActionBindingManager) {

		bindings.bindTrigger(grabAction)
			.bindGripPose(palmPoseAction)
			.bindThumbstick(moveAction)
			.bindThumbstickX(lookAction)
			.bindButtonRightA(upAction)
			.bindButtonRightB(downAction)
			.bindHaptic(hapticAction)
	}

	override fun bindToSession(stack: MemoryStack, session: OpenXrSession) {
		super.bindToSession(stack, session)
		palmPoseAction.baseSpace = session.space
	}
}