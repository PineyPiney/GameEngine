package com.pineypiney.game_engine_test.xr

import com.pineypiney.game_engine.resources.DirectorySource
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.xr.*
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class TestOpenXrEngine(window: WindowI, api: XrGraphicsApi, val screen: (TestOpenXrEngine) -> OpenXrTest = ::OpenXrTest): OpenXrGameEngine<OpenXrTest>(window, api) {

	override val actionSet: TestActions = TestActions()
	override var openxr: OpenXrManager
	override lateinit var activeScreen: OpenXrTest private set

	val supportsHandTracking: Boolean
	val supportsSimultaneousInput: Boolean
	lateinit var hands: Array<HandTracker>

	init {
		val extensions = listOf(
			EXTDebugUtils.XR_EXT_DEBUG_UTILS_EXTENSION_NAME,
			EXTHandTracking.XR_EXT_HAND_TRACKING_EXTENSION_NAME, EXTHandInteraction.XR_EXT_HAND_INTERACTION_EXTENSION_NAME,
			EXTHandJointsMotionRange.XR_EXT_HAND_JOINTS_MOTION_RANGE_EXTENSION_NAME, FBHandTrackingMesh.XR_FB_HAND_TRACKING_MESH_EXTENSION_NAME,
			METASimultaneousHandsAndControllers.XR_META_SIMULTANEOUS_HANDS_AND_CONTROLLERS_EXTENSION_NAME,
		)
		openxr = OpenXrManager(window, xrGraphicsApi, actionSet, extensions)

		MemoryStack.stackPush().use { stack ->
			val handTracking = XrSystemHandTrackingPropertiesEXT.calloc(stack).`type$Default`()
			val simultInput = XrSystemSimultaneousHandsAndControllersPropertiesMETA.calloc(stack).`type$Default`()
			val systemProperties = XrSystemProperties.calloc(stack).`type$Default`().next(handTracking).next(simultInput)
			XrUtil.getSystemProperties(openxr.instance, openxr.systemID, systemProperties)
			supportsHandTracking = handTracking.supportsHandTracking()
			supportsSimultaneousInput = simultInput.supportsSimultaneousHandsAndControllers()

			if(supportsHandTracking) {
				hands = XrUtil.createHandTrackers(stack, openxr)
				deletionQueue.pushArray(hands)
			}
		}
	}

	override fun init() {
		super.init()
		if(supportsSimultaneousInput) {
			val info = XrSimultaneousHandsAndControllersTrackingResumeInfoMETA.calloc().`type$Default`()
			METASimultaneousHandsAndControllers.xrResumeSimultaneousHandsAndControllersTrackingMETA(openxr.session.session, info)
			info.free()
		}
	}

	override fun createResourcesLoader(factory: ResourceFactory): ResourcesLoader = ResourcesLoader(factory, listOf(DirectorySource("src/main/resources"), DirectorySource("src/test/resources")))

	override fun setLogic() {
		activeScreen = screen(this)
		deletionQueue.push(activeScreen)
	}
}