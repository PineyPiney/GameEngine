package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.ptr
import glm_.quat.Quat
import glm_.vec3.Vec3
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class OpenXrSession(val session: XrSession, val manager: OpenXrManager) : Deletable {

	val eventData = XrEventDataBuffer.calloc().`type$Default`()
	var running = false
	var state = XR10.XR_SESSION_STATE_UNKNOWN
	val active get() = state in XR10.XR_SESSION_STATE_SYNCHRONIZED..XR10.XR_SESSION_STATE_FOCUSED

	val leftHandPath: Long
	val rightHandPath: Long

	var space: XrSpace private set
	val viewSpace: XrSpace

	init {
		MemoryStack.stackPush().use { stack ->
			leftHandPath = XrUtil.createPath(stack, manager.instance, "/user/hand/left")
			rightHandPath = XrUtil.createPath(stack, manager.instance, "/user/hand/right")
			space = XrUtil.createReferenceSpace(stack, session, XR10.XR_REFERENCE_SPACE_TYPE_LOCAL)
			viewSpace = XrUtil.createReferenceSpace(stack, session, XR10.XR_REFERENCE_SPACE_TYPE_VIEW)
		}

		manager.nameObject(space.address(), XR10.XR_OBJECT_TYPE_SPACE, "OpenXR Local Space")
		manager.nameObject(viewSpace.address(), XR10.XR_OBJECT_TYPE_SPACE, "OpenXR View Space")
	}

	fun begin(viewConfiguration: Int){
		MemoryStack.stackPush().use { stack ->
			val sessionBeginInfo = XrSessionBeginInfo.calloc(stack).`type$Default`()
				.primaryViewConfigurationType(viewConfiguration)
			XrUtil.processResult(XR10.xrBeginSession(session, sessionBeginInfo), "Failed to begin Session.")
		}
		running = true
	}

	fun setSpaceOffset(pos: Vec3, rot: Quat){
		XrUtil.processResult(XR10.xrDestroySpace(space), "Failed to destroy XrSpace $space")
		MemoryStack.stackPush().use { stack ->
			space = XrUtil.createReferenceSpace(stack, session, XR10.XR_REFERENCE_SPACE_TYPE_LOCAL, pos, rot)
		}
		manager.actionSet.setSpace(space)
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

	fun attachActionSets(stack: MemoryStack, actionSets: List<XrActionSet>){
		val attachInfo = XrSessionActionSetsAttachInfo.calloc(stack).`type$Default`()
			.actionSets(stack.ptr(actionSets))

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
					GameEngineI.logger.info("OPENXR: Interaction Profile changed for Session: ${interactionProfileChanged.session()}")
					if (interactionProfileChanged.session() != session.address()) {
						GameEngineI.logger.warn("XrEventDataInteractionProfileChanged for unknown Session")
						return true
					}
					recordBindings()
				}
				// Log that there's a reference space change pending.
				XR10.XR_TYPE_EVENT_DATA_REFERENCE_SPACE_CHANGE_PENDING -> {
					val referenceSpaceChangePending = XrEventDataReferenceSpaceChangePending.create(eventData.address())
					GameEngineI.logger.warn("OPENXR: Reference Space Change pending for Session: ${referenceSpaceChangePending.session()}")
					if (referenceSpaceChangePending.session() != session.address()) {
						GameEngineI.logger.warn("XrEventDataReferenceSpaceChangePending for unknown Session")
					}
				}
				// Session State changes:
				XR10.XR_TYPE_EVENT_DATA_SESSION_STATE_CHANGED -> {
					val sessionStateChanged = XrEventDataSessionStateChanged.create(eventData.address())
					if (sessionStateChanged.session() != session.address()) {
						GameEngineI.logger.warn("XrEventDataSessionStateChanged for unknown Session")
						continue
					}
					if (!onStateChange(sessionStateChanged.state())) return false
				}
			}
		}
		return true
	}

	fun pollActions(predictedTime: Long) {
		MemoryStack.stackPush().use { stack ->

			val activeSets = XrActiveActionSet.malloc(1, stack)
				.actionSet(manager.actionSet.actionSet)
				.subactionPath(XR10.XR_NULL_PATH)

			val syncInfo = XrActionsSyncInfo.calloc(stack).`type$Default`()
				.activeActionSets(activeSets)
				.countActiveActionSets(1)

			XrUtil.processResult(XR10.xrSyncActions(session, syncInfo), "Failed to sync action sets")

			val getInfo = XrActionStateGetInfo.calloc(stack).`type$Default`()
			for(action in manager.actionSet.inputActions) action.update(getInfo, session, predictedTime)
		}
	}

	fun onStateChange(state: Int): Boolean {

		this.state = state

		return when(state){
			XR10.XR_SESSION_STATE_READY -> {
				// SessionState is ready. Begin the XrSession using the XrViewConfigurationType.
				GameEngineI.logger.info("Starting OpenXR Session")
				begin(manager.renderManager.viewConfiguration)
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
				GameEngineI.logger.info("Exiting OpenXR Session")
				running = false
				false
			}
			XR10.XR_SESSION_STATE_LOSS_PENDING -> {
				// SessionState is loss pending. Exit the application.
				// It's possible to try and reestablish an XrInstance and XrSession, but we will simply exit here.
				running = false
				false
			}
			else -> true
		}
	}

	fun exit(){
		if(running) XrUtil.processResult(XR10.xrRequestExitSession(session), "Failed to quest session exit")
		else state = 9
	}

	override fun delete() {
		XrUtil.processResult(XR10.xrDestroySpace(space), "Failed to destroy Reference Space")
		XrUtil.processResult(XR10.xrDestroySpace(viewSpace), "Failed to destroy Reference Space")

		eventData.free()
		XrUtil.processResult(XR10.xrDestroySession(session), "Failed to destroy Session")
	}
}