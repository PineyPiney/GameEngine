package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.GameRenderer
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlArrayMesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlMesh
import com.pineypiney.game_engine.rendering.opengl.OpenGlFramebuffer
import com.pineypiney.game_engine.rendering.opengl.OpenGlRendering
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.opengl.OpenGlRenderShader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.rigidInverse
import com.pineypiney.game_engine.window.GLFWWindow
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import kool.set
import org.lwjgl.PointerBuffer
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL31C
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

// Use OpenXR Simulator at https://github.com/elliotttate/OpenXR-Simulator
// Run ../OpenXR-Simulator/scripts/register-runtime.ps1 to set it as OpenXR Runtime
class OpenXrManager(val window: GLFWWindow) : Deletable {

	val instance: XrInstance

	val errorHandle: XrDebugUtilsMessengerEXT

	val systemID: Long

	val viewConfiguration: Int
	val viewConfigurationViews: List<XrViewConfigurationView>
	val blendMode: Int

	val actionSet: XrActionSet
	val grabAction: XrAction
	val palmPoseAction: XrAction
	val hapticAction: XrAction

	val session: OpenXrSession

	val space: XrSpace

	val swapchainFormats: List<Long>
	val swapchains: List<OpenXrSwapchain>


	init {

//		nameObject(instance.address(), XR10.XR_OBJECT_TYPE_INSTANCE, "OpenXR Instance")
//		nameObject(errorHandle.address(), EXTDebugUtils.XR_OBJECT_TYPE_DEBUG_UTILS_MESSENGER_EXT, "Error Messenger")
//		nameObject(session.session.address(), XR10.XR_OBJECT_TYPE_SESSION, "OpenXR Session")

	    MemoryStack.stackPush().use { stack ->
			instance = XrUtil.createInstance(stack)
			errorHandle = XrUtil.setupDebugger(stack, instance, EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or EXTDebugUtils.XR_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT, ::errorCallback)
			systemID = XrUtil.getSystemId(stack, instance)

			viewConfiguration = XrUtil.getViewConfigurations(stack, instance, systemID).firstOrNull { it == XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO } ?: XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_MONO
			viewConfigurationViews = XrUtil.getViewConfigurationViews(stack, instance, systemID, viewConfiguration)

			val availableBlendModes = XrUtil.getBlendModes(stack, instance, systemID, viewConfiguration)
			blendMode = blendModes.firstOrNull{ availableBlendModes.contains(it) } ?: XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE

			actionSet = XrUtil.createActionSet(stack, instance, "openxr-test-action_set", "OpenXR Test Action Set")
			grabAction = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_FLOAT_INPUT, listOf("/user/hand/left", "/user/hand/right"), "grab_cube", "Grab Cube")
			palmPoseAction = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_POSE_INPUT, listOf("/user/hand/left", "/user/hand/right"), "palm_pose", "Palm Pose")
			hapticAction = XrUtil.createAction(stack, actionSet, XR10.XR_ACTION_TYPE_VIBRATION_OUTPUT, listOf("/user/hand/left", "/user/hand/right"), "haptic", "Haptic")

			val suggestedKhronosBindings = XrUtil.createSuggestedBindings(stack, listOf(
				grabAction to "/user/hand/left/input/select/click",
				grabAction to "/user/hand/right/input/select/click",
				palmPoseAction to "/user/hand/left/input/grip/pose",
				palmPoseAction to "/user/hand/right/input/grip/pose",
				hapticAction to "/user/hand/left/output/haptic",
				hapticAction to "/user/hand/right/output/haptic",
			))
			val suggestedOculusBindings = XrUtil.createSuggestedBindings(stack, listOf(
				grabAction to "/user/hand/left/input/squeeze/value",
				grabAction to "/user/hand/right/input/squeeze/value",
				palmPoseAction to "/user/hand/left/input/grip/pose",
				palmPoseAction to "/user/hand/right/input/grip/pose",
				hapticAction to "/user/hand/left/output/haptic",
				hapticAction to "/user/hand/right/output/haptic",
			))
			val successfulSuggestion =
				XrUtil.suggestBindings(stack, instance, "/interaction_profiles/khr/simple_controller", suggestedKhronosBindings) ||
						XrUtil.suggestBindings(stack, instance, "/interaction_profiles/oculus/touch_controller", suggestedOculusBindings)
			if(!successfulSuggestion){
				GameEngineI.logger.error("Failed to suggest bindings")
			}

			session = XrUtil.createOpenGlWin32Session(stack, this, window)
			space = XrUtil.createReferenceSpace(stack, session.session)

			swapchainFormats = XrUtil.getSwapchainFormats(stack, session.session)
			val colourFormat = openglColourFormats.first { swapchainFormats.contains(it.toLong()) }.toLong()
			val depthFormat = openglDepthFormats.first { swapchainFormats.contains(it.toLong()) }.toLong()
			swapchains = XrUtil.createSwapchains(stack, session.session, viewConfigurationViews, colourFormat, depthFormat)
		}

//		for((i, swapchain) in swapchains.withIndex()) {
//			nameObject(swapchain.colour.address(), XR10.XR_OBJECT_TYPE_SWAPCHAIN, "OpenXR Colour Swapchain $i")
//			nameObject(swapchain.depth.address(), XR10.XR_OBJECT_TYPE_SWAPCHAIN, "OpenXR Depth Swapchain $i")
//		}
//		nameObject(space.address(), XR10.XR_OBJECT_TYPE_SPACE, "OpenXR Space")
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
		val name = MemoryUtil.memUTF8(name)
		MemoryStack.stackPush().use { stack ->
			val info = XrDebugUtilsObjectNameInfoEXT.calloc(stack).`type$Default`()
				.objectType(type)
				.objectHandle(ptr)
				.objectName(name)
			EXTDebugUtils.xrSetDebugUtilsObjectNameEXT(instance, info)
		}
	}

	fun renderFrame(){
		MemoryStack.stackPush().use { stack ->
			val frameState = XrFrameState.calloc(stack).`type$Default`()
			val waitInfo = XrFrameWaitInfo.calloc(stack).`type$Default`()
			XrUtil.processResult(XR10.xrWaitFrame(session.session, waitInfo, frameState), "Failed to wait for frame")

			val beginInfo = XrFrameBeginInfo.calloc(stack).`type$Default`()
			XrUtil.processResult(XR10.xrBeginFrame(session.session, beginInfo), "Failed to begin frame")

			val layerPointers = mutableListOf<Long>()

			if(session.active && frameState.shouldRender()){
				session.pollActions(frameState.predictedDisplayTime())
				val pointer = stack.mallocPointer(1)
				if(renderLayer(stack, frameState.predictedDisplayTime(), pointer)){
					layerPointers.add(pointer[0])
				}
			}

			val layers = stack.mallocPointer(layerPointers.size)
			for(layer in layerPointers) layers.put(layer)

			val endInfo = XrFrameEndInfo.calloc(stack).`type$Default`()
				.displayTime(frameState.predictedDisplayTime())
				.environmentBlendMode(blendMode)
				.layerCount(layers.capacity())
				.layers(layers.flip())


			XrUtil.processResult(XR10.xrEndFrame(session.session, endInfo), "Failed to end frame")
		}
	}

	fun renderLayer(stack: MemoryStack, displayTime: Long, compositionLayerPtr: PointerBuffer): Boolean {
		val views = XrView.calloc(viewConfigurationViews.size, stack)
		for(view in views) view.`type$Default`()

		val viewState = XrViewState.calloc(stack).`type$Default`()
		val locateInfo = XrViewLocateInfo.calloc(stack).`type$Default`()
			.viewConfigurationType(viewConfiguration)
			.displayTime(displayTime)
			.space(space)

		val b = stack.mallocInt(1)
		val result = XR10.xrLocateViews(session.session, locateInfo, viewState, b, views)
		val viewCount = b[0]
		if(XrUtil.isFail(result)) {
			GameEngineI.logger.warn("Failed to locate views: ${XrUtil.translateOpenXrResult(result)}")
			return false
		}

		val compositionLayer = XrCompositionLayerProjection.calloc(stack).`type$Default`()
		val compositionLayerViews = XrCompositionLayerProjectionView.calloc(viewCount, stack)

		repeat(viewCount){ i ->
			val swapchain = swapchains[i]

			val acquireInfo = XrSwapchainImageAcquireInfo.calloc(stack).`type$Default`()
			XrUtil.processResult(XR10.xrAcquireSwapchainImage(swapchain.colour, acquireInfo, b), "Failed to get swapchain colour image")
			val colourImage = b[0]
			XrUtil.processResult(XR10.xrAcquireSwapchainImage(swapchain.depth, acquireInfo, b), "Failed to get swapchain depth image")
			swapchain.framebuffer.setTextures(colourImage, b[0])

			val waitInfo = XrSwapchainImageWaitInfo.calloc(stack).`type$Default`()
				.timeout(XR10.XR_INFINITE_DURATION)
			XrUtil.processResult(XR10.xrWaitSwapchainImage(swapchain.colour, waitInfo), "Failed to wait for swapchain colour image")
			XrUtil.processResult(XR10.xrWaitSwapchainImage(swapchain.depth, waitInfo), "Failed to wait for swapchain depth image")

			val width = viewConfigurationViews[i].recommendedImageRectWidth()
			val height = viewConfigurationViews[i].recommendedImageRectHeight()
			GLFunc.viewportO = Vec2i(width, height)

			compositionLayerViews[i].`type$Default`()
				.pose(views[i].pose())
				.fov(views[i].fov())
				.subImage { subimage ->
					subimage.swapchain(swapchain.colour)
						.imageRect { rect -> rect.set(0, 0, width, height) }
						.imageArrayIndex(0)
				}

			GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, swapchain.framebuffer.FBO)
			GLFunc.clearColour = Vec4(.8f, .2f, .8f, 1f)
			GLFunc.depthClear = 1.0
			GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT or GL11C.GL_DEPTH_BUFFER_BIT)

			val xrView = views[i]
			val (view, projection) = getMatrices(xrView)

			cubeMesh.bind()
			GLFunc.depthTest = true
			GLFunc.cullFace = false
			shader.use(OpenGlRendering)
			shader.setMat4("view", view)
			shader.setMat4("projection", projection)

			for(i in 0..99){
				shader.setMat4("model", Mat4().scale(.15f).translate(-4.5f + (i % 10), -4.5f + (i / 10), 0f))
				shader.setVec4("colour", Vec4((i % 10) * .1f, (i / 10) * .1f, 0f, 1f))
				cubeMesh.draw(OpenGlRendering)
			}

			shader.setVec4("colour", Vec4(0f, 0f, 1f, 1f))

			shader.setMat4("model", session.leftHandPose.mat4().scale(.1f))
			cubeMesh.draw(OpenGlRendering)

			shader.setMat4("model", session.rightHandPose.mat4().scale(.1f))
			cubeMesh.draw(OpenGlRendering)


			val releaseInfo = XrSwapchainImageReleaseInfo.calloc(stack).`type$Default`()
			XrUtil.processResult(XR10.xrReleaseSwapchainImage(swapchain.colour, releaseInfo), "Failed to release swapchain colour image")
			XrUtil.processResult(XR10.xrReleaseSwapchainImage(swapchain.depth, releaseInfo), "Failed to release swapchain depth image")

			OpenGlFramebuffer.unbind()
			val w = window.width / viewCount
			GLFunc.viewportO = window.framebufferSize
			GLFunc.clearColour = Vec4(1f - i, 0f + i, 0, 1f)
//			GL13C.glClear(GL13C.GL_COLOR_BUFFER_BIT)
			GameRenderer.screenShader.use(OpenGlRendering)
			swapchain.framebuffer.colourTextures[colourImage].bind()
			(Mesh.screenQuadShape as OpenGlMesh).bindAndDraw(OpenGlRendering)
		}

		compositionLayer.layerFlags((XR10.XR_COMPOSITION_LAYER_BLEND_TEXTURE_SOURCE_ALPHA_BIT or XR10.XR_COMPOSITION_LAYER_CORRECT_CHROMATIC_ABERRATION_BIT).toLong())
			.space(space)
			.views(compositionLayerViews)

        compositionLayerPtr[0] = compositionLayer.address()
		return true
	}

	fun getMatrices(view: XrView): Pair<Mat4, Mat4> {
		val projection = XrUtil.createProjection(view.fov(), .05f, 100f, false)
		val view = view.pose().mat4()
		return view.rigidInverse(view) to projection
	}

	override fun delete() {
		swapchains.delete()
		XrUtil.processResult(XR10.xrDestroySpace(space), "Failed to destroy Reference Space")
		session.delete()
		XrUtil.processResult(EXTDebugUtils.xrDestroyDebugUtilsMessengerEXT(errorHandle), "Failed to destroy Debug Messenger")
		XrUtil.processResult(XR10.xrDestroyInstance(instance), "Failed to destroy Instance")
	}

	companion object {
		// https://openxr-tutorial.com//linux/opengl/3-graphics.html#create-the-swapchains
		val openglColourFormats = listOf(GL31C.GL_RGB10_A2, GL31C.GL_RGBA16F, GL31C.GL_RGBA8, GL31C.GL_RGBA8_SNORM)
		val openglDepthFormats = listOf(GL31C.GL_DEPTH_COMPONENT32F, GL31C.GL_DEPTH_COMPONENT32, GL31C.GL_DEPTH_COMPONENT24, GL31C.GL_DEPTH_COMPONENT16)

		val blendModes = listOf(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)

		val shader = ShaderLoader.loadRenderShader(ResourceKey("3d/colour")) as OpenGlRenderShader
		val cubeMesh = Mesh.centerCubeShape as OpenGlArrayMesh

		var INVERT_VIEW = true
		var NEG_PROJ = false
		var TRANS_PROJ = false
	}
}