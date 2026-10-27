package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.rendering.openxr.OpenXrRenderManager
import com.pineypiney.game_engine.rendering.openxr.OpenXrVulkanManager
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.extension_functions.toString
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.xr.actions.ActionBindingManager
import com.pineypiney.game_engine.xr.actions.OpenXrActionSet
import glm_.vec2.Vec2i
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

// Use OpenXR Simulator at https://github.com/elliotttate/OpenXR-Simulator
// Run ../OpenXR-Simulator/scripts/register-runtime.ps1 to set it as OpenXR Runtime

// This class is used to handle cross-api OpenXR runtime, with GraphicsApi-specific functionality handled by the graphicsApi
class OpenXrManager(
	val window: Window,
	val graphicsApi: XrGraphicsApi,
	val actionSet: OpenXrActionSet,
	extensions: Collection<String>,
	renderManagerFact: (session: OpenXrSession, List<Int>) -> OpenXrRenderManager = ::OpenXrRenderManager,
	blendModes: List<Int> = listOf(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
) : Initialisable {

	val deletionQueue = OpenXrDeletionQueue()

	val instance: XrInstance

	val systemID: Long

	val manager: ApiManager
	val session: OpenXrSession
	val renderManager: OpenXrRenderManager

	init {

		enabledLayers.addAll("XR_APILAYER_LUNARG_core_validation", "XR_APILAYER_KHRONOS_best_practices_validation")
		enabledExtensions.addAll(graphicsApi.getExtensions())
		enabledExtensions.addAll(extensions)

	    MemoryStack.stackPush().use { stack ->
			instance = XrUtil.createInstance(stack, "Piney's Game Engine", enabledLayers, enabledExtensions)
			deletionQueue.pushHandle(instance, XR10::xrDestroyInstance)
			if(debugEnabled) {
				val errorHandle = XrUtil.setupDebugger(stack, instance, EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT, ::errorCallback)
				deletionQueue.pushHandle(errorHandle, EXTDebugUtils::xrDestroyDebugUtilsMessengerEXT)
			}

			val (name, version) = XrUtil.getInstanceProperties(stack, instance)
			GameEngineI.logger.info("Started OpenXR Runtime $name ${version.toString(".", Int::toString)}")
			systemID = XrUtil.getSystemId(stack, instance)

			// getRequirements must be called before creating a Vulkan Instance
			val (minVersion, maxVersion) = graphicsApi.getRequirements(stack, instance, systemID)
			GameEngineI.logger.info("OpenXR requires ${graphicsApi.getName()} ${Vec2i(minVersion).toString(".", Int::toString)} to ${Vec2i(maxVersion).toString(".", Int::toString)}")

			actionSet.init(stack, this)
			deletionQueue.push(actionSet)

			manager = graphicsApi.createApi("OpenXR Test", instance, systemID, window.api)
			manager.init()
			println(manager.printInfo())
			deletionQueue.push(manager)


			var successfulSuggestion = false
			val bindings = ActionBindingManager(instance)
			actionSet.bindActions(bindings)
			for((profile, bindings) in bindings.bind(stack)) successfulSuggestion = XrUtil.suggestBindings(stack, instance, profile, bindings) || successfulSuggestion
			if(!successfulSuggestion){
				GameEngineI.logger.error("Failed to suggest bindings")
			}

			session = graphicsApi.createSession(stack, this, manager, window)
			actionSet.bindToSession(stack, session)
			actionSet.setSpace(session.space)
			session.attachActionSets(stack, listOf(actionSet.actionSet))
			deletionQueue.push(session)

			renderManager = renderManagerFact(session, blendModes)
			deletionQueue.push(renderManager)

			if(manager is OpenXrVulkanManager){
				manager.colourFormat = renderManager.colourFormat
				manager.depthFormat = renderManager.depthFormat
			}
		}


		nameObject(instance.address(), XR10.XR_OBJECT_TYPE_INSTANCE, "OpenXR Instance")
//		nameObject(errorHandle.address(), EXTDebugUtils.XR_OBJECT_TYPE_DEBUG_UTILS_MESSENGER_EXT, "Error Messenger")
		nameObject(session.session.address(), XR10.XR_OBJECT_TYPE_SESSION, "OpenXR Session")
	}

	override fun init() {
		renderManager.init()
	}

	@Suppress("unused")
	fun errorCallback(severity: Long, type: Long, callbackData: Long, userData: Long): Int {
		val data = XrDebugUtilsMessengerCallbackDataEXT.create(callbackData)
		when (severity.toInt()) {
			EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> {
				GameEngineI.logger.error("OpenXR Error Occurred: " + data.messageString())
			}
			EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> {
				GameEngineI.logger.warn("OpenXR Warning Occurred: " + data.messageString())
			}
			EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT -> {
				GameEngineI.logger.debug("OpenXR Debug Message Occurred: " + data.messageString())
			}
			else -> {
				GameEngineI.logger.debug("OpenXR Info Message Occurred: " + data.messageString())
			}
		}
		return 0
	}

	fun nameObject(ptr: Long, type: Int, name: String) {
		if(EXTDebugUtils.XR_EXT_DEBUG_UTILS_EXTENSION_NAME !in enabledExtensions) return
		val name = MemoryUtil.memUTF8(name)
		MemoryStack.stackPush().use { stack ->
			val info = XrDebugUtilsObjectNameInfoEXT.calloc(stack).`type$Default`()
				.objectType(type)
				.objectHandle(ptr)
				.objectName(name)
			EXTDebugUtils.xrSetDebugUtilsObjectNameEXT(instance, info)
		}
	}

	override fun delete() {
		deletionQueue.flush()
	}

	companion object {
		val enabledLayers = mutableListOf<String>()
		val enabledExtensions = mutableListOf<String>()

		val debugEnabled get() = EXTDebugUtils.XR_EXT_DEBUG_UTILS_EXTENSION_NAME in enabledExtensions
		val depthEnabled get() = KHRCompositionLayerDepth.XR_KHR_COMPOSITION_LAYER_DEPTH_EXTENSION_NAME in enabledExtensions
	}
}