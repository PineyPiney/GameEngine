package com.pineypiney.game_engine_test.xr

import com.pineypiney.game_engine.resources.DirectorySource
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.xr.*
import glm_.has
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class TestOpenXrEngine(window: Window, api: XrGraphicsApi, val screen: (TestOpenXrEngine) -> OpenXrTest = ::OpenXrTest): OpenXrGameEngine<OpenXrTest>(window, api) {

	override val actionSet: TestActions = TestActions()
	override var openxr: OpenXrManager
	override lateinit var activeScreen: OpenXrTest private set

	lateinit var hands: Array<HandTracker>

	init {
		val extensions = listOf(
			EXTDebugUtils.XR_EXT_DEBUG_UTILS_EXTENSION_NAME,
			KHRCompositionLayerDepth.XR_KHR_COMPOSITION_LAYER_DEPTH_EXTENSION_NAME,
			EXTHandTracking.XR_EXT_HAND_TRACKING_EXTENSION_NAME, EXTHandInteraction.XR_EXT_HAND_INTERACTION_EXTENSION_NAME,

			// Meta/Oculus Specific Extensions
			FBPassthrough.XR_FB_PASSTHROUGH_EXTENSION_NAME,
			EXTHandJointsMotionRange.XR_EXT_HAND_JOINTS_MOTION_RANGE_EXTENSION_NAME, FBHandTrackingMesh.XR_FB_HAND_TRACKING_MESH_EXTENSION_NAME,
			FBRenderModel.XR_FB_RENDER_MODEL_EXTENSION_NAME,
			METASimultaneousHandsAndControllers.XR_META_SIMULTANEOUS_HANDS_AND_CONTROLLERS_EXTENSION_NAME,
		)
		openxr = OpenXrManager(window, xrGraphicsApi, actionSet, extensions, ::TestRenderManager, listOf(XR10.XR_ENVIRONMENT_BLEND_MODE_ALPHA_BLEND))

		MemoryStack.stackPush().use { stack ->
			val handTracking = XrSystemHandTrackingPropertiesEXT.calloc(stack).`type$Default`()
			val simultInput = XrSystemSimultaneousHandsAndControllersPropertiesMETA.calloc(stack).`type$Default`()
			val passThrough = XrSystemPassthroughProperties2FB.calloc(stack).`type$Default`()
			val systemProperties = XrSystemProperties.calloc(stack).`type$Default`().next(handTracking).next(simultInput).next(passThrough)
			XrUtil.getSystemProperties(openxr.instance, openxr.systemID, systemProperties)
			supportsHandTracking = handTracking.supportsHandTracking()
			supportsSimultaneousInput = simultInput.supportsSimultaneousHandsAndControllers()
			supportsPassthrough = passThrough.capabilities().toInt() has FBPassthrough.XR_PASSTHROUGH_CAPABILITY_BIT_FB

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

	companion object {
		var supportsHandTracking: Boolean = false; private set
		var supportsSimultaneousInput: Boolean = false; private set
		var supportsPassthrough: Boolean = false; private set
	}
}