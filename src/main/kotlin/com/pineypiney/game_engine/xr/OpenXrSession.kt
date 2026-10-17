package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.LwjglUtil
import com.pineypiney.game_engine.util.extension_functions.hasAll
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class OpenXrSession(val session: XrSession, val manager: OpenXrManager) : Deletable {

	val eventData = XrEventDataBuffer.calloc().`type$Default`()
	var running = false
	var state = XR10.XR_SESSION_STATE_UNKNOWN
	val active get() = state in XR10.XR_SESSION_STATE_SYNCHRONIZED..XR10.XR_SESSION_STATE_FOCUSED

	val leftHandPath: Long
	val rightHandPath: Long
	val leftHandPoseSpace: XrSpace
	val rightHandPoseSpace: XrSpace
	val leftHandPose: XrPosef = XrPosef.calloc()
	val rightHandPose: XrPosef = XrPosef.calloc()
	val leftHandState = XrActionStatePose.calloc().`type$Default`()
	val rightHandState = XrActionStatePose.calloc().`type$Default`()
	val leftHandGrabState = XrActionStateFloat.calloc().`type$Default`()
	val rightHandGrabState = XrActionStateFloat.calloc().`type$Default`()

	init {
		MemoryStack.stackPush().use { stack ->
			leftHandPath = XrUtil.createPath(stack, session.instance, "/user/hand/left")
			rightHandPath = XrUtil.createPath(stack, session.instance, "/user/hand/right")

			leftHandPoseSpace = XrUtil.createActionPoseSpace(stack, session, manager.palmPoseAction, leftHandPath)
			rightHandPoseSpace = XrUtil.createActionPoseSpace(stack, session, manager.palmPoseAction, rightHandPath)

			attachActionSet(stack, listOf(manager.actionSet))
		}
	}

	fun begin(viewConfiguration: Int){
		MemoryStack.stackPush().use { stack ->
			val sessionBeginInfo = XrSessionBeginInfo.calloc(stack).`type$Default`()
				.primaryViewConfigurationType(viewConfiguration)
			XrUtil.processResult(XR10.xrBeginSession(session, sessionBeginInfo), "Failed to begin Session.")
		}
		running = true
	}

	fun end(){
		XrUtil.processResult(XR10.xrEndSession(session), "Failed to end Session.")
		running = false
	}

	fun recordBindings(){
		MemoryStack.stackPush().use { stack ->

			val profile = XrInteractionProfileState.calloc(stack).`type$Default`()
			XrUtil.processResult(XR10.xrGetCurrentInteractionProfile(session, leftHandPath, profile), "Failed to get left hand profile")
			if(profile.interactionProfile() != 0L) GameEngineI.logger.info("OpenXR InteractionProfile /user/hand/left: ${XrUtil.readPath(stack, session.instance, profile.interactionProfile())}")

			XrUtil.processResult(XR10.xrGetCurrentInteractionProfile(session, rightHandPath, profile), "Failed to get right hand profile")
			if(profile.interactionProfile() != 0L) GameEngineI.logger.info("OpenXR InteractionProfile /user/hand/right: ${XrUtil.readPath(stack, session.instance, profile.interactionProfile())}")
		}
	}

	fun attachActionSet(stack: MemoryStack, actionSets: List<XrActionSet>){
		val attachInfo = XrSessionActionSetsAttachInfo.calloc(stack).`type$Default`()
			.actionSets(LwjglUtil.createPointerBuffer(stack, actionSets))

		XrUtil.processResult(XR10.xrAttachSessionActionSets(session, attachInfo), "Failed to attach action sets.")
	}

	fun pollEvents(): Boolean{
		while (XR10.xrPollEvent(manager.instance, eventData.type(XR10.XR_TYPE_EVENT_DATA_BUFFER)) == XR10.XR_SUCCESS) {
			when (eventData.type()) {
				XR10.XR_TYPE_EVENT_DATA_EVENTS_LOST -> {
					val eventsLost = XrEventDataEventsLost.create(eventData.address())
					GameEngineI.logger.warn("OPENXR: Events Lost: ${eventsLost.lostEventCount()}")
				}
				// Log that an instance loss is pending and shutdown the application.
				XR10.XR_TYPE_EVENT_DATA_INSTANCE_LOSS_PENDING -> {
					val instanceLossPending = XrEventDataInstanceLossPending.create(eventData.address())
					GameEngineI.logger.warn("OPENXR: Instance Loss Pending at: ${instanceLossPending.lossTime()}")
				}
				// Log that the interaction profile has changed.
				XR10.XR_TYPE_EVENT_DATA_INTERACTION_PROFILE_CHANGED -> {
					val interactionProfileChanged = XrEventDataInteractionProfileChanged.create(eventData.address())
					GameEngineI.logger.warn("OPENXR: Interaction Profile changed for Session: ${interactionProfileChanged.session()}")
					if (interactionProfileChanged.session() != manager.session.session.address()) {
						GameEngineI.logger.warn("XrEventDataInteractionProfileChanged for unknown Session")
						return true
					}
					recordBindings()
				}
				// Log that there's a reference space change pending.
				XR10.XR_TYPE_EVENT_DATA_REFERENCE_SPACE_CHANGE_PENDING -> {
					val referenceSpaceChangePending = XrEventDataReferenceSpaceChangePending.create(eventData.address())
					GameEngineI.logger.warn("OPENXR: Reference Space Change pending for Session: ${referenceSpaceChangePending.session()}")
					if (referenceSpaceChangePending.session() != manager.session.session.address()) {
						GameEngineI.logger.warn("XrEventDataReferenceSpaceChangePending for unknown Session")
					}
				}
				// Session State changes:
				XR10.XR_TYPE_EVENT_DATA_SESSION_STATE_CHANGED -> {
					val sessionStateChanged = XrEventDataSessionStateChanged.create(eventData.address())
					if (sessionStateChanged.session() != manager.session.session.address()) {
						GameEngineI.logger.warn("XrEventDataSessionStateChanged for unknown Session")
						continue
					}
					if (!manager.session.onStateChange(sessionStateChanged.state())) return false
				}
			}
		}
		return true
	}

	fun pollActions(predictedTime: Long) {
		MemoryStack.stackPush().use { stack ->

			val activeSet = XrActiveActionSet.malloc(1, stack)
				.actionSet(manager.actionSet)
				.subactionPath(XR10.XR_NULL_PATH)

			val syncInfo = XrActionsSyncInfo.calloc(stack).`type$Default`()
				.activeActionSets(activeSet)
				.countActiveActionSets(1)

			XrUtil.processResult(XR10.xrSyncActions(session, syncInfo), "Failed to sync action sets")

			val getInfo = XrActionStateGetInfo.calloc(stack).`type$Default`()
				.action(manager.palmPoseAction)

			pollPoseAction(stack, predictedTime, getInfo, leftHandPath, leftHandPoseSpace, leftHandPose, leftHandState)
			pollPoseAction(stack, predictedTime, getInfo, rightHandPath, rightHandPoseSpace, rightHandPose, rightHandState)

			getInfo.action(manager.grabAction)

			pollFloatAction(getInfo, leftHandPath, leftHandGrabState)
			pollFloatAction(getInfo, rightHandPath, rightHandGrabState)

			val vibration = XrStructs.hapticVibration(stack, leftHandGrabState.currentState() * .5f)
			val actionInfo = XrHapticActionInfo.calloc(stack).`type$Default`().action(manager.hapticAction)
			hapticFeedback(actionInfo, leftHandPath, XrHapticBaseHeader.create(vibration))
			hapticFeedback(actionInfo, rightHandPath, XrHapticBaseHeader.create(vibration.amplitude(rightHandGrabState.currentState() * .5f)))
		}
	}

	fun pollPoseAction(stack: MemoryStack, predictedTime: Long, getInfo: XrActionStateGetInfo, path: Long, space: XrSpace, pose: XrPosef, poseState: XrActionStatePose){
		getInfo.subactionPath(path)
		XrUtil.processResult(XR10.xrGetActionStatePose(session, getInfo, poseState), "Failed to get pose")
		if(poseState.isActive) {
			val location = XrSpaceLocation.calloc(stack).`type$Default`()
			val res = XR10.xrLocateSpace(space, manager.space, predictedTime, location)
			if(res == 0 && location.locationFlags().toInt() hasAll (XR10.XR_SPACE_LOCATION_POSITION_VALID_BIT or XR10.XR_SPACE_LOCATION_ORIENTATION_VALID_BIT)) {
				pose.set(location.pose())
			}
			else poseState.isActive(false)
		}
	}

	fun pollFloatAction(getInfo: XrActionStateGetInfo, path: Long, state: XrActionStateFloat) {
		getInfo.subactionPath(path).subactionPath(path)
		XrUtil.processResult(XR10.xrGetActionStateFloat(session, getInfo, state), "Failed to get float")
	}

	fun hapticFeedback(actionInfo: XrHapticActionInfo, path: Long, vibration: XrHapticBaseHeader){
		actionInfo.subactionPath(path)
		XrUtil.processResult(XR10.xrApplyHapticFeedback(session, actionInfo, vibration), "Failed to apply haptic feedback")
	}

	fun onStateChange(state: Int): Boolean {

		this.state = state

		return when(state){
			XR10.XR_SESSION_STATE_READY -> {
				// SessionState is ready. Begin the XrSession using the XrViewConfigurationType.
				GameEngineI.logger.info("Starting OpenXR Session")
				begin(manager.viewConfiguration)
				true
			}
			XR10.XR_SESSION_STATE_STOPPING -> {
				// SessionState is stopping. End the XrSession.
				GameEngineI.logger.info("Ending OpenXR Session")
				end()
				true
			}
			XR10.XR_SESSION_STATE_EXITING -> {
				// SessionState is exiting. Exit the application.
				running = false
				false
			}
			XR10.XR_SESSION_STATE_LOSS_PENDING -> {
				// SessionState is loss pending. Exit the application.
				// It's possible to try a reestablish an XrInstance and XrSession, but we will simply exit here.
				running = false
				false
			}
			else -> true
		}
	}

	override fun delete() {
		leftHandPose.free()
		rightHandPose.free()
		leftHandState.free()
		rightHandState.free()
		leftHandGrabState.free()
		rightHandGrabState.free()

		eventData.free()
		XrUtil.processResult(XR10.xrDestroySession(session), "Failed to destroy Session")
	}
}