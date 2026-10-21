package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.LwjglUtil
import com.pineypiney.game_engine.util.LwjglUtil.enumerate
import com.pineypiney.game_engine.util.LwjglUtil.enumerateStructs
import com.pineypiney.game_engine.util.extension_functions.stringsToPointer
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.window.WindowI
import glm_.detail.GLM_DEPTH_CLIP_SPACE
import glm_.detail.GlmDepthClipSpace
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import kool.map
import org.lwjgl.glfw.GLFWNativeWGL
import org.lwjgl.glfw.GLFWNativeWayland
import org.lwjgl.glfw.GLFWNativeWin32
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.windows.User32
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.math.tan

object XrUtil {


	fun translateOpenXrResult(result: Int): String {
		return when (result) {

			XR10.XR_SUCCESS -> "Function successfully completed."

			// Warnings are > 0
			XR10.XR_TIMEOUT_EXPIRED -> "The specified timeout time occurred before the operation could complete."
			XR10.XR_SESSION_LOSS_PENDING -> "The session will be lost soon."
			XR10.XR_EVENT_UNAVAILABLE -> "No event was available."
			XR10.XR_SPACE_BOUNDS_UNAVAILABLE -> "The space’s bounds are not known at the moment."
			XR10.XR_SESSION_NOT_FOCUSED -> "The session is not in the focused state."
			XR10.XR_FRAME_DISCARDED -> "A frame has been discarded from composition."
			FBRenderModel.XR_RENDER_MODEL_UNAVAILABLE_FB -> "The model is unavailable."
			MSFTSceneMarker.XR_SCENE_MARKER_DATA_NOT_STRING_MSFT -> "Marker does not encode a string."
			METAEnvironmentDepth.XR_ENVIRONMENT_DEPTH_NOT_AVAILABLE_META -> "Warning: The requested depth image is not yet available."
			MLFacialExpression.XR_ERROR_FACIAL_EXPRESSION_PERMISSION_DENIED_ML -> "Permission to track facial expressions was not granted."
			METAColocationDiscovery.XR_COLOCATION_DISCOVERY_ALREADY_ADVERTISING_META -> "Colocation advertisement has already been enabled."
			METAColocationDiscovery.XR_COLOCATION_DISCOVERY_ALREADY_DISCOVERING_META -> "Colocation discovery has already been enabled."

			// Errors are < 0
			XR10.XR_ERROR_VALIDATION_FAILURE -> "The function usage was invalid in some way."
			XR10.XR_ERROR_RUNTIME_FAILURE -> "The runtime failed to handle the function in an unexpected way that is not covered by another error result."
			XR10.XR_ERROR_OUT_OF_MEMORY -> "A memory allocation has failed."
			XR10.XR_ERROR_API_VERSION_UNSUPPORTED -> "The runtime does not support the requested API version."
			XR10.XR_ERROR_INITIALIZATION_FAILED -> "Initialization of object could not be completed."
			XR10.XR_ERROR_FUNCTION_UNSUPPORTED -> "The requested function was not found or is otherwise unsupported."
			XR10.XR_ERROR_FEATURE_UNSUPPORTED -> "The requested feature is not supported."
			XR10.XR_ERROR_EXTENSION_NOT_PRESENT -> "A requested extension is not supported."
			XR10.XR_ERROR_LIMIT_REACHED -> "The runtime supports no more of the requested resource."
			XR10.XR_ERROR_SIZE_INSUFFICIENT -> "The supplied size was smaller than required."
			XR10.XR_ERROR_HANDLE_INVALID -> "A supplied object handle was invalid."
			XR10.XR_ERROR_INSTANCE_LOST -> "The XrInstance was lost or could not be found. It will need to be destroyed and optionally recreated."
			XR10.XR_ERROR_SESSION_RUNNING -> "The session is already running</a>."
			XR10.XR_ERROR_SESSION_NOT_RUNNING -> "The session is not yet running</a>."
			XR10.XR_ERROR_SESSION_LOST -> "The XrSession was lost. It will need to be destroyed and optionally recreated."
			XR10.XR_ERROR_SYSTEM_INVALID -> "The provided XrSystemId was invalid."
			XR10.XR_ERROR_PATH_INVALID -> "The provided XrPath was not valid."
			XR10.XR_ERROR_PATH_COUNT_EXCEEDED -> "The maximum number of supported semantic paths has been reached."
			XR10.XR_ERROR_PATH_FORMAT_INVALID -> "The semantic path character format is invalid."
			XR10.XR_ERROR_PATH_UNSUPPORTED -> "The semantic path is unsupported."
			XR10.XR_ERROR_LAYER_INVALID -> "The layer was NULL or otherwise invalid."
			XR10.XR_ERROR_LAYER_LIMIT_EXCEEDED -> "The number of specified layers is greater than the supported number."
			XR10.XR_ERROR_SWAPCHAIN_RECT_INVALID -> "The image rect was negatively sized or otherwise invalid."
			XR10.XR_ERROR_SWAPCHAIN_FORMAT_UNSUPPORTED -> "The image format is not supported by the runtime or platform."
			XR10.XR_ERROR_ACTION_TYPE_MISMATCH -> "The API used to retrieve an action’s state does not match the action’s type."
			XR10.XR_ERROR_SESSION_NOT_READY -> "The session is not in the ready state."
			XR10.XR_ERROR_SESSION_NOT_STOPPING -> "The session is not in the stopping state."
			XR10.XR_ERROR_TIME_INVALID -> "The provided XrTime was zero, negative, or out of range."
			XR10.XR_ERROR_REFERENCE_SPACE_UNSUPPORTED -> "The specified reference space is not supported by the runtime or system."
			XR10.XR_ERROR_FILE_ACCESS_ERROR -> "The file could not be accessed."
			XR10.XR_ERROR_FILE_CONTENTS_INVALID -> "The file’s contents were invalid."
			XR10.XR_ERROR_FORM_FACTOR_UNSUPPORTED -> "The specified form factor is not supported by the current runtime or platform."
			XR10.XR_ERROR_FORM_FACTOR_UNAVAILABLE -> "The specified form factor is supported, but the device is currently not available, e.g. not plugged in or powered off."
			XR10.XR_ERROR_API_LAYER_NOT_PRESENT -> "A requested API layer is not present or could not be loaded."
			XR10.XR_ERROR_CALL_ORDER_INVALID -> "The call was made without having made a previously required call."
			XR10.XR_ERROR_GRAPHICS_DEVICE_INVALID -> "The given graphics device is not in a valid state. The graphics device could be lost or initialized without meeting graphics requirements."
			XR10.XR_ERROR_POSE_INVALID -> "The supplied pose was invalid with respect to the requirements."
			XR10.XR_ERROR_INDEX_OUT_OF_RANGE -> "The supplied index was outside the range of valid indices."
			XR10.XR_ERROR_VIEW_CONFIGURATION_TYPE_UNSUPPORTED -> "The specified view configuration type is not supported by the runtime or platform."
			XR10.XR_ERROR_ENVIRONMENT_BLEND_MODE_UNSUPPORTED -> "The specified environment blend mode is not supported by the runtime or platform."
			XR10.XR_ERROR_NAME_DUPLICATED -> "The name provided was a duplicate of an already-existing resource."
			XR10.XR_ERROR_NAME_INVALID -> "The name provided was invalid."
			XR10.XR_ERROR_ACTIONSET_NOT_ATTACHED -> "A referenced action set is not attached to the session."
			XR10.XR_ERROR_ACTIONSETS_ALREADY_ATTACHED -> "The session already has attached action sets."
			XR10.XR_ERROR_LOCALIZED_NAME_DUPLICATED -> "The localized name provided was a duplicate of an already-existing resource."
			XR10.XR_ERROR_LOCALIZED_NAME_INVALID -> "The localized name provided was invalid."
			XR10.XR_ERROR_GRAPHICS_REQUIREMENTS_CALL_MISSING -> "The xrGetGraphicsRequirements call was not made before calling xrCreateSession."
			XR10.XR_ERROR_RUNTIME_UNAVAILABLE -> "The loader was unable to find or load a runtime."

			XR11.XR_ERROR_EXTENSION_DEPENDENCY_NOT_ENABLED -> "One or more of the extensions being enabled has dependency on extensions that are not enabled."
			XR11.XR_ERROR_PERMISSION_INSUFFICIENT -> "Insufficient permissions. This error is included for use by vendor extensions. The precise definition of XR_ERROR_PERMISSION_INSUFFICIENT and actions possible by the developer or user to resolve it can vary by platform, extension or function. The developer should refer to the documentation of the function."
			MSFTSpatialAnchor.XR_ERROR_CREATE_SPATIAL_ANCHOR_FAILED_MSFT -> "Spatial anchor could not be created at that location."
			MSFTSecondaryViewConfiguration.XR_ERROR_SECONDARY_VIEW_CONFIGURATION_TYPE_NOT_ENABLED_MSFT -> "The secondary view configuration was not enabled when creating the session."
			MSFTControllerModel.XR_ERROR_CONTROLLER_MODEL_KEY_INVALID_MSFT -> "The controller model key is invalid."
			MSFTCompositionLayerReprojection.XR_ERROR_REPROJECTION_MODE_UNSUPPORTED_MSFT -> "The reprojection mode is not supported."
			MSFTSceneUnderstanding.XR_ERROR_COMPUTE_NEW_SCENE_NOT_COMPLETED_MSFT -> "Compute new scene not completed."
			MSFTSceneUnderstanding.XR_ERROR_SCENE_COMPONENT_ID_INVALID_MSFT -> "Scene component id invalid."
			MSFTSceneUnderstanding.XR_ERROR_SCENE_COMPONENT_TYPE_MISMATCH_MSFT -> "Scene component type mismatch."
			MSFTSceneUnderstanding.XR_ERROR_SCENE_MESH_BUFFER_ID_INVALID_MSFT -> "Scene mesh buffer id invalid."
			MSFTSceneUnderstanding.XR_ERROR_SCENE_COMPUTE_FEATURE_INCOMPATIBLE_MSFT -> "Scene compute feature incompatible."
			MSFTSceneUnderstanding.XR_ERROR_SCENE_COMPUTE_CONSISTENCY_MISMATCH_MSFT -> "Scene compute consistency mismatch."
			FBDisplayRefreshRate.XR_ERROR_DISPLAY_REFRESH_RATE_UNSUPPORTED_FB -> "The display refresh rate is not supported by the platform."
			FBColorSpace.XR_ERROR_COLOR_SPACE_UNSUPPORTED_FB -> "The color space is not supported by the runtime."
			FBSpatialEntity.XR_ERROR_SPACE_COMPONENT_NOT_SUPPORTED_FB -> "The component type is not supported for this space."
			FBSpatialEntity.XR_ERROR_SPACE_COMPONENT_NOT_ENABLED_FB -> "The required component is not enabled for this space."
			FBSpatialEntity.XR_ERROR_SPACE_COMPONENT_STATUS_PENDING_FB -> "A request to set the component’s status is currently pending."
			FBSpatialEntity.XR_ERROR_SPACE_COMPONENT_STATUS_ALREADY_SET_FB -> "The component is already set to the requested value."
			FBPassthrough.XR_ERROR_UNEXPECTED_STATE_PASSTHROUGH_FB -> "The object state is unexpected for the issued command."
			FBPassthrough.XR_ERROR_FEATURE_ALREADY_CREATED_PASSTHROUGH_FB -> "Trying to create an MR feature when one was already created and only one instance is allowed."
			FBPassthrough.XR_ERROR_FEATURE_REQUIRED_PASSTHROUGH_FB -> "Requested functionality requires a feature to be created first."
			FBPassthrough.XR_ERROR_NOT_PERMITTED_PASSTHROUGH_FB -> "Requested functionality is not permitted - application is not allowed to perform the requested operation."
			FBPassthrough.XR_ERROR_INSUFFICIENT_RESOURCES_PASSTHROUGH_FB -> "There were insufficient resources available to perform an operation."
			FBPassthrough.XR_ERROR_UNKNOWN_PASSTHROUGH_FB -> "Unknown Passthrough error (no further details provided)."
			FBRenderModel.XR_ERROR_RENDER_MODEL_KEY_INVALID_FB -> "The model key is invalid."
			VARJOMarkerTracking.XR_ERROR_MARKER_NOT_TRACKED_VARJO -> "Marker tracking is disabled or the specified marker is not currently tracked."
			VARJOMarkerTracking.XR_ERROR_MARKER_ID_INVALID_VARJO -> "The specified marker ID is not valid."
			MLMarkerUnderstanding.XR_ERROR_MARKER_DETECTOR_PERMISSION_DENIED_ML -> "The com.magicleap.permission.MARKER_TRACKING permission was denied."
			MLMarkerUnderstanding.XR_ERROR_MARKER_DETECTOR_LOCATE_FAILED_ML -> "The specified marker could not be located spatially."
			MLMarkerUnderstanding.XR_ERROR_MARKER_DETECTOR_INVALID_DATA_QUERY_ML -> "The marker queried does not contain data of the requested type."
			MLMarkerUnderstanding.XR_ERROR_MARKER_DETECTOR_INVALID_CREATE_INFO_ML -> "createInfo contains mutually exclusive parameters, such as setting {@link MLMarkerUnderstanding#XR_MARKER_DETECTOR_CORNER_REFINE_METHOD_APRIL_TAG_ML MARKER_DETECTOR_CORNER_REFINE_METHOD_APRIL_TAG_ML} with {@link MLMarkerUnderstanding#XR_MARKER_TYPE_ARUCO_ML MARKER_TYPE_ARUCO_ML}."
			MLMarkerUnderstanding.XR_ERROR_MARKER_INVALID_ML -> "The marker id passed to the function was invalid."
			MLLocalizationMap.XR_ERROR_LOCALIZATION_MAP_INCOMPATIBLE_ML -> "The localization map being imported is not compatible with current OS or mode."
			MLLocalizationMap.XR_ERROR_LOCALIZATION_MAP_UNAVAILABLE_ML -> "The localization map requested is not available."
			MLLocalizationMap.XR_ERROR_LOCALIZATION_MAP_FAIL_ML -> "The map localization service failed to fulfill the request, retry later."
			MLLocalizationMap.XR_ERROR_LOCALIZATION_MAP_IMPORT_EXPORT_PERMISSION_DENIED_ML -> "The com.magicleap.permission.SPACE_IMPORT_EXPORT permission was denied."
			MLLocalizationMap.XR_ERROR_LOCALIZATION_MAP_PERMISSION_DENIED_ML -> "The com.magicleap.permission.SPACE_MANAGER permission was denied."
			MLLocalizationMap.XR_ERROR_LOCALIZATION_MAP_ALREADY_EXISTS_ML -> "The map being imported already exists in the system."
			MLLocalizationMap.XR_ERROR_LOCALIZATION_MAP_CANNOT_EXPORT_CLOUD_MAP_ML -> "The map localization service cannot export cloud based maps."
			MLSpatialAnchors.XR_ERROR_SPATIAL_ANCHORS_PERMISSION_DENIED_ML -> "The com.magicleap.permission.SPATIAL_ANCHOR permission was not granted."
			MLSpatialAnchors.XR_ERROR_SPATIAL_ANCHORS_NOT_LOCALIZED_ML -> "Operation failed because the system is not localized into a localization map."
			MLSpatialAnchors.XR_ERROR_SPATIAL_ANCHORS_OUT_OF_MAP_BOUNDS_ML -> "Operation failed because it is performed outside of the localization map."
			MLSpatialAnchors.XR_ERROR_SPATIAL_ANCHORS_SPACE_NOT_LOCATABLE_ML -> "Operation failed because the space referenced cannot be located."
			MLSpatialAnchorsStorage.XR_ERROR_SPATIAL_ANCHORS_ANCHOR_NOT_FOUND_ML -> "The anchor references was not found."
			MSFTSpatialAnchorPersistence.XR_ERROR_SPATIAL_ANCHOR_NAME_NOT_FOUND_MSFT -> "A spatial anchor was not found associated with the spatial anchor name provided"
			MSFTSpatialAnchorPersistence.XR_ERROR_SPATIAL_ANCHOR_NAME_INVALID_MSFT -> "The spatial anchor name provided was not valid"
			FBSpatialEntitySharing.XR_ERROR_SPACE_MAPPING_INSUFFICIENT_FB -> "Anchor import from cloud or export from device failed."
			FBSpatialEntitySharing.XR_ERROR_SPACE_LOCALIZATION_FAILED_FB -> "Anchors were downloaded from the cloud but failed to be imported/aligned on the device."
			FBSpatialEntitySharing.XR_ERROR_SPACE_NETWORK_TIMEOUT_FB -> "Timeout occurred while waiting for network request to complete."
			FBSpatialEntitySharing.XR_ERROR_SPACE_NETWORK_REQUEST_FAILED_FB -> "The network request failed."
			FBSpatialEntitySharing.XR_ERROR_SPACE_CLOUD_STORAGE_DISABLED_FB -> "Cloud storage is required for this operation but is currently disabled."
			METAPassthroughColorLut.XR_ERROR_PASSTHROUGH_COLOR_LUT_BUFFER_SIZE_MISMATCH_META -> "The provided data buffer did not match the required size."
			QCOMTrackingOptimizationSettings.XR_ERROR_HINT_ALREADY_SET_QCOM -> "Tracking optimization hint is already set for the domain."
			HTCAnchor.XR_ERROR_NOT_AN_ANCHOR_HTC -> "The provided space is valid but not an anchor."
			EXTPlaneDetection.XR_ERROR_SPACE_NOT_LOCATABLE_EXT -> "The space passed to the function was not locatable."
			EXTPlaneDetection.XR_ERROR_PLANE_DETECTION_PERMISSION_DENIED_EXT -> "The permission for this resource was not granted."
			EXTFuture.XR_ERROR_FUTURE_PENDING_EXT -> "Returned by completion function to indicate future is not ready."
			EXTFuture.XR_ERROR_FUTURE_INVALID_EXT -> "Returned by completion function to indicate future is not valid."
			MLSystemNotifications.XR_ERROR_SYSTEM_NOTIFICATION_PERMISSION_DENIED_ML -> "The com.magicleap.permission.SYSTEM_NOTIFICATION permission was not granted."
			MLSystemNotifications.XR_ERROR_SYSTEM_NOTIFICATION_INCOMPATIBLE_SKU_ML -> "Incompatible SKU detected."
			MLWorldMeshDetection.XR_ERROR_WORLD_MESH_DETECTOR_PERMISSION_DENIED_ML -> "The world mesh detector permission was not granted."
			MLWorldMeshDetection.XR_ERROR_WORLD_MESH_DETECTOR_SPACE_NOT_LOCATABLE_ML -> "At the time of the call the runtime was unable to locate the space and cannot fulfill your request."
			METAColocationDiscovery.XR_ERROR_COLOCATION_DISCOVERY_NETWORK_FAILED_META -> "The network request failed."
			METAColocationDiscovery.XR_ERROR_COLOCATION_DISCOVERY_NO_DISCOVERY_METHOD_META -> "The runtime does not have any methods available to perform discovery."
			METASpatialEntityGroupSharing.XR_ERROR_SPACE_GROUP_NOT_FOUND_META -> "The group UUID was not be found within the runtime"
			else -> String.format("%s [%d]", "Unknown", result)
		}
	}

	fun isFail(res: Int) = res != XR10.XR_SUCCESS

	@Throws(AssertionError::class)
	fun processResult(res: Int, message: String) {
		if (res > XR10.XR_SUCCESS) {
			GameEngineI.logger.warn("$message: ${translateOpenXrResult(res)}")
		} else if (res < XR10.XR_SUCCESS) {
			throw AssertionError("$message: ${translateOpenXrResult(res)}")
		}
	}

	fun getAvailableLayers(stack: MemoryStack): List<String> {
		val b = stack.mallocInt(1)
		processResult(XR10.xrEnumerateApiLayerProperties(b, null), "Failed to get the number of available layers")

		if (b[0] > 0) {
			val layerProps = XrApiLayerProperties.malloc(b[0], stack)
			for(prop in layerProps) prop.`type$Default`()
			processResult(XR10.xrEnumerateApiLayerProperties(b, layerProps), "Failed to get available layers")
			return layerProps.map { it.layerNameString() }
		}
		return emptyList()
	}


	fun getAvailableExtensions(stack: MemoryStack): List<String> {
		val b = stack.callocInt(1)
		processResult(XR10.xrEnumerateInstanceExtensionProperties(null as ByteBuffer?, b, null), "Failed to get the number of available extensions")

		if (b[0] > 0) {
			val extProps = XrExtensionProperties.malloc(b[0], stack)
			for(prop in extProps) prop.`type$Default`()
			processResult(XR10.xrEnumerateInstanceExtensionProperties(null as ByteBuffer?, b, extProps), "Failed to get available extensions")
			return extProps.map { it.extensionNameString() }
		}
		return emptyList()
	}

	fun getViewConfigurations(stack: MemoryStack, instance: XrInstance, systemId: Long): List<Int> {
		val result = enumerate(stack, "View Configurations", instance, systemId, MemoryStack::mallocInt, XR10::xrEnumerateViewConfigurations) ?: return emptyList()

		return result.map{ it }
	}

	fun getViewConfigurationViews(stack: MemoryStack, instance: XrInstance, systemId: Long, viewConfiguration: Int): List<XrViewConfigurationView> {
		val result = enumerateStructs(stack, "View Configuration Views", XrViewConfigurationView::`type$Default`, XrViewConfigurationView::calloc){ b, res ->
			XR10.xrEnumerateViewConfigurationViews(instance, systemId, viewConfiguration, b, res)
		} ?: return emptyList()

		return result.map{ XrViewConfigurationView.malloc().set(it) }
	}

	fun getBlendModes(stack: MemoryStack, instance: XrInstance, systemId: Long, viewType: Int): List<Int> {
		val result = enumerate(stack, "Blend Modes", instance, systemId, MemoryStack::mallocInt){ ins, sys, b, res ->
			XR10.xrEnumerateEnvironmentBlendModes(ins, sys, viewType, b, res)
		} ?: return emptyList()
		return result.map{ it }
	}

	fun getSwapchainFormats(stack: MemoryStack, session: XrSession): List<Long> {
		val result = enumerate(stack, "Swapchain Formats", session, MemoryStack::mallocLong, XR10::xrEnumerateSwapchainFormats) ?: return emptyList()
		return result.map{ it }
	}

	fun getOpenGlSwapchainImages(stack: MemoryStack, swapchain: XrSwapchain): List<XrSwapchainImageOpenGLKHR> {
		val result = enumerateStructs(stack, "Swapchain Images", XrSwapchainImageOpenGLKHR::`type$Default`, XrSwapchainImageOpenGLKHR::calloc){ b, res ->
			XR10.xrEnumerateSwapchainImages(swapchain, b, res?.let(XrSwapchainImageBaseHeader::create))
		} ?: return emptyList()
		return result.map{ it }
	}

	fun getVulkanSwapchainImages(stack: MemoryStack, swapchain: XrSwapchain): List<XrSwapchainImageVulkanKHR> {
		val result = enumerateStructs(stack, "Swapchain Images", XrSwapchainImageVulkanKHR::`type$Default`, XrSwapchainImageVulkanKHR::calloc){ b, res ->
			XR10.xrEnumerateSwapchainImages(swapchain, b, res?.let(XrSwapchainImageBaseHeader::create))
		} ?: return emptyList()
		return result.map{ it }
	}

	fun createInstance(stack: MemoryStack, name: String = "Piney's GameEngine", layers: MutableList<String>, extensions: MutableList<String>, version: Int = 0): XrInstance{
		val appName = if(name.length > XR10.XR_MAX_APPLICATION_NAME_SIZE) name.substring(0, XR10.XR_MAX_APPLICATION_NAME_SIZE) else name
		val engineName = "Piney's GameEngine"
//		if(engineName.length > XR10.XR_MAX_ENGINE_NAME_SIZE) return null

		layers.retainAll(getAvailableLayers(stack))
		extensions.retainAll(getAvailableExtensions(stack))

		val layersPointer = stack.stringsToPointer(layers)
		val extensionsPointer = stack.stringsToPointer(extensions)

		val createInfo = XrInstanceCreateInfo.calloc(stack).`type$Default`()
			.createFlags(0)
			.applicationInfo{ info -> info.set(stack.UTF8(appName), version, stack.UTF8(engineName), 1, XR10.XR_API_VERSION_1_0) }
			.enabledApiLayerNames(layersPointer)
			.enabledExtensionNames(extensionsPointer)

		val b = stack.mallocPointer(1)
   		processResult(XR10.xrCreateInstance(createInfo, b), "Failed to create OpenXR Instance")
		return XrInstance(b[0], createInfo)
	}

	fun getInstanceProperties(stack: MemoryStack, instance: XrInstance): Pair<String, Vec3i> {
		val props = XrInstanceProperties.calloc(stack).`type$Default`()
		processResult(XR10.xrGetInstanceProperties(instance, props), "Failed to get instance properties")
		return props.runtimeNameString() to LwjglUtil.version(props.runtimeVersion())
	}

	fun setupDebugger(stack: MemoryStack, instance: XrInstance, severities: Int, callback: XrDebugUtilsMessengerCallbackEXTI): XrDebugUtilsMessengerEXT {
		val b = stack.mallocPointer(1)
		val createInfo = XrDebugUtilsMessengerCreateInfoEXT.calloc(stack).`type$Default`()
			.messageSeverities(severities.toLong())
			.messageTypes((EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT).toLong())
			.userCallback(callback)
		processResult(EXTDebugUtils.xrCreateDebugUtilsMessengerEXT(instance, createInfo, b), "Failed to create Debug Messenger")
		return XrDebugUtilsMessengerEXT(b[0], instance)
	}

	fun getSystemId(stack: MemoryStack, instance: XrInstance): Long {

		val getInfo = XrSystemGetInfo.calloc(stack).`type$Default`()
			.formFactor(XR10.XR_FORM_FACTOR_HEAD_MOUNTED_DISPLAY)

		val b = stack.mallocLong(1)
		processResult(XR10.xrGetSystem(instance, getInfo, b), "Failed to get System Id")
		val properties = XrSystemProperties.malloc(stack).`type$Default`()
		processResult(XR10.xrGetSystemProperties(instance, b[0], properties), "Failed to get System Properties")
		return b[0]
	}

	fun createVulkanSession(stack: MemoryStack, manager: OpenXrManager, vulkan: VulkanManager): OpenXrSession {

		val api = XrGraphicsBindingVulkan2KHR.calloc(stack).`type$Default`()
			.instance(vulkan.instance)
			.physicalDevice(vulkan.gpu.physicalDevice)
			.device(vulkan.device.device).queueFamilyIndex(vulkan.device.queueFamilyIndex)
			.queueIndex(0)

		val createInfo = XrStructs.sessionInfo(stack, manager.systemID)
			.next(api)

		return createSession(stack, createInfo, manager)
	}

	fun createOpenGlWin32Session(stack: MemoryStack, manager: OpenXrManager, window: WindowI): OpenXrSession {

		// https://medium.com/@javiercarranzaburgueno/an-easy-openxr-vr-implementation-for-c-games-93c161321a3b#3bda
		val hWND = GLFWNativeWin32.glfwGetWin32Window(window.windowHandle)
		val hDC = User32.GetDC(hWND)
		val hGLRC = GLFWNativeWGL.glfwGetWGLContext(window.windowHandle)

		val api = XrGraphicsBindingOpenGLWin32KHR.calloc(stack).`type$Default`()
			.hDC(hDC)
			.hGLRC(hGLRC)

		val createInfo = XrStructs.sessionInfo(stack, manager.systemID)
			.next(api)

		return createSession(stack, createInfo, manager)
	}

	fun createOpenGlWaylandSession(stack: MemoryStack, manager: OpenXrManager): OpenXrSession {

		val display = GLFWNativeWayland.glfwGetWaylandDisplay()

		val api = XrGraphicsBindingOpenGLWaylandKHR.calloc(stack).`type$Default`()
			.display(display)

		val createInfo = XrStructs.sessionInfo(stack, manager.systemID)
			.next(api)

		return createSession(stack, createInfo, manager)
	}

	fun createSession(stack: MemoryStack, createInfo: XrSessionCreateInfo, manager: OpenXrManager): OpenXrSession {

		val b = stack.mallocPointer(1)
		processResult(XR10.xrCreateSession(manager.instance, createInfo, b), "Failed to create OpenXR session")
		return OpenXrSession(XrSession(b[0], manager.instance), manager)
	}

	fun createSwapchains(stack: MemoryStack, session: OpenXrSession, views: List<XrViewConfigurationView>, colourFormat: Long, depthFormat: Long): List<OpenXrSwapchain> {
		val createInfo = XrSwapchainCreateInfo.calloc(stack).`type$Default`()
		return views.map { view ->
			createInfo.createFlags(1)
				.usageFlags((XR10.XR_SWAPCHAIN_USAGE_SAMPLED_BIT or XR10.XR_SWAPCHAIN_USAGE_COLOR_ATTACHMENT_BIT).toLong())
				.format(colourFormat)
				.sampleCount(view.recommendedSwapchainSampleCount())
				.width(view.recommendedImageRectWidth())
				.height(view.recommendedImageRectHeight())
				.faceCount(1)
				.arraySize(1)
				.mipCount(1)

			val swapchainPtr = stack.mallocPointer(1)
			processResult(XR10.xrCreateSwapchain(session.session, createInfo, swapchainPtr), "Failed to create Colour Swapchain")
			val colourSwapchain = XrSwapchain(swapchainPtr[0], session.session)

			createInfo.createFlags(1)
				.usageFlags((XR10.XR_SWAPCHAIN_USAGE_SAMPLED_BIT or XR10.XR_SWAPCHAIN_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT).toLong())
				.format(depthFormat)
				.sampleCount(view.recommendedSwapchainSampleCount())
				.width(view.recommendedImageRectWidth())
				.height(view.recommendedImageRectHeight())
				.faceCount(1)
				.arraySize(1)
				.mipCount(1)

			processResult(XR10.xrCreateSwapchain(session.session, createInfo, swapchainPtr), "Failed to create Depth Swapchain")
			val depthSwapchain = XrSwapchain(swapchainPtr[0], session.session)

			val framebuffer = session.manager.graphicsApi.createFramebuffer(stack, session.manager.manager, createInfo.width(), createInfo.height(), colourSwapchain, depthSwapchain, colourFormat.toInt(), depthFormat.toInt())
			OpenXrSwapchain(view, colourSwapchain, depthSwapchain, framebuffer)
		}
	}

	fun createReferenceSpace(stack: MemoryStack, session: XrSession, type: Int, initialPos: Vec3 = Vec3(0f), initialRot: Quat = Quat()): XrSpace {
		val createInfo = XrReferenceSpaceCreateInfo.calloc(stack).`type$Default`()
			.referenceSpaceType(type)
			.poseInReferenceSpace{ it.set(initialRot, initialPos) }
		val space = stack.mallocPointer(1)
		processResult(XR10.xrCreateReferenceSpace(session, createInfo, space), "Failed to create Reference Space")
		return XrSpace(space[0], session)
	}

	fun createPath(stack: MemoryStack, instance: XrInstance, str: String): Long {
		val b = stack.mallocLong(1)
		XR10.xrStringToPath(instance, stack.UTF8(str), b)
		return b[0]
	}

	fun readPath(stack: MemoryStack, instance: XrInstance, path: Long): String {
		val bytes = enumerate(stack, "XrPath", instance, path, MemoryStack::malloc, XR10::xrPathToString) ?: return "Empty Path"
		return MemoryUtil.memUTF8(bytes, bytes.remaining() - 1)
	}

	fun createActionSet(stack: MemoryStack, instance: XrInstance, name: String, localizedName: String, priority: Int = 0): XrActionSet {
		val createInfo = XrActionSetCreateInfo.calloc(stack).`type$Default`()
			.priority(priority)
		createInfo.actionSetName().asCharBuffer().put(name, 0, min(name.length, XR10.XR_MAX_ACTION_SET_NAME_SIZE))
		createInfo.localizedActionSetName().asCharBuffer().put(localizedName, 0, min(localizedName.length, XR10.XR_MAX_LOCALIZED_ACTION_SET_NAME_SIZE))

		val b = stack.mallocPointer(1)
		processResult(XR10.xrCreateActionSet(instance, createInfo, b), "Failed to create Action Set")
		return XrActionSet(b[0], instance)
	}

	fun createAction(stack: MemoryStack, set: XrActionSet, type: Int, subActionPaths: Collection<Long>, name: String, localizedName: String): XrAction {
		val l = stack.mallocLong(subActionPaths.size)
		for(path in subActionPaths) l.put(path)
		val createInfo = XrActionCreateInfo.calloc(stack).`type$Default`()
			.actionType(type)
			.subactionPaths(l.flip())
			.countSubactionPaths(l.remaining())

		createInfo.actionName().asCharBuffer().put(name, 0, min(name.length, XR10.XR_MAX_ACTION_NAME_SIZE))
		createInfo.localizedActionName().asCharBuffer().put(localizedName, 0, min(localizedName.length, XR10.XR_MAX_LOCALIZED_ACTION_NAME_SIZE))

		val b = stack.mallocPointer(1)
		processResult(XR10.xrCreateAction(set, createInfo, b), "Failed to create Action")
		return XrAction(b[0], set)
	}

	fun suggestBindings(stack: MemoryStack, instance: XrInstance, profile: String, suggestions: XrActionSuggestedBinding.Buffer): Boolean {

		val profileSuggestions = XrInteractionProfileSuggestedBinding.calloc(stack).`type$Default`()
			.interactionProfile(createPath(stack, instance, profile))
			.suggestedBindings(suggestions)

		if(isFail(XR10.xrSuggestInteractionProfileBindings(instance, profileSuggestions))){
			GameEngineI.logger.error("Failed to suggest bindings for profile $profile")
			return false
		}
		else return true
	}

	fun createSuggestedBindings(stack: MemoryStack, suggestions: Collection<Pair<XrAction, String>>): XrActionSuggestedBinding.Buffer {
		val bindings = XrActionSuggestedBinding.calloc(suggestions.size, stack)
		for((action, path) in suggestions) {
			bindings.get().set(action, createPath(stack, action.actionSet.instance, path))
		}
		return bindings.flip()
	}

	fun createActionPoseSpace(stack: MemoryStack, session: XrSession, action: XrAction, path: Long) : XrSpace {
		val createInfo = XrActionSpaceCreateInfo.calloc(stack).`type$Default`()
			.action(action)
			.poseInActionSpace { it.reset() }
			.subactionPath(path)

		val b = stack.mallocPointer(1)
		processResult(XR10.xrCreateActionSpace(session, createInfo, b), "Failed to create Action Space")
		return XrSpace(b[0], session)
	}

	fun createProjection(fov: XrFovf, near: Float, far: Float, invertY: Boolean, res: Mat4 = Mat4()): Mat4 {
		val tanLeft = tan(fov.angleLeft())
		val tanRight = tan(fov.angleRight())
		val tanUp = tan(fov.angleUp())
		val tanDown = tan(fov.angleDown())

		val tanWidth = tanRight - tanLeft
		val tanHeight = if(invertY) tanDown - tanUp else tanUp - tanDown
		val zOffset = if(GLM_DEPTH_CLIP_SPACE == GlmDepthClipSpace.NEGATIVE_ONE_TO_ONE) near else 0f

		val m22: Float
		val m23: Float
		// Place far plane at infinity
		if(false){
			m22 = -1f
			m23 = -(near + zOffset)
		}
		else {
			val invSpan = 1f / (near - far)
			m22 = (far + zOffset) * invSpan
			m23 = far * (near + zOffset) * invSpan
		}

		return res(
			2f / tanWidth, 					0f, 							0f, 0f,
			0f, 								2f / tanHeight, 				0f, 0f,
			(tanLeft + tanRight) / tanWidth, 	(tanDown + tanUp) / tanHeight, m22, -1f,
			0f, 								0f, 							m23, 0f
		)
	}
}