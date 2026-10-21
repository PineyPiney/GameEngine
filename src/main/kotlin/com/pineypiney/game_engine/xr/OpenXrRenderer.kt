package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.rigidInverse
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowI
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kool.set
import org.lwjgl.PointerBuffer
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class OpenXrRenderer(window: WindowI, val session: OpenXrSession) : RendererI {

	val viewOffset = Vec3(0f)
	override val viewPos: Vec3 = Vec3(0f)
	override val view: Mat4 = I
	override val projection: Mat4 = I
	override val guiProjection: Mat4 = I
	override val viewportSize: Vec2i = Vec2i(1280, 1400)
	override val aspectRatio: Float get() = viewportSize.x.toFloat() / viewportSize.y.toFloat()

	val instance get() = session.manager.instance
	val systemID get() = session.manager.systemID
	val space get() = session.space

	val api: PresentingApi = session.manager.manager.createPresentingApi(window, "OpenXR Renderer")

	val viewConfiguration: Int
	val blendMode: Int

	val colourFormat: TextureFormat
	val depthFormat: TextureFormat
	val swapchainFormats: List<Long>
	val swapchains: List<OpenXrSwapchain>

	init {
		MemoryStack.stackPush().use { stack ->
			viewConfiguration = XrUtil.getViewConfigurations(stack, instance, systemID).firstOrNull { it == XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO } ?: XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_MONO
			val viewConfigurationViews = XrUtil.getViewConfigurationViews(stack, instance, systemID, viewConfiguration)

			val availableBlendModes = XrUtil.getBlendModes(stack, instance, systemID, viewConfiguration)
			blendMode = blendModes.firstOrNull{ availableBlendModes.contains(it) } ?: XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE

			swapchainFormats = XrUtil.getSwapchainFormats(stack, session.session)
			val formats = TextureFormat.entries.filter { swapchainFormats.contains(session.manager.manager.getEnum(it).toLong()) }

			val colourFormats = session.manager.graphicsApi.getColourFormats()
			colourFormat = colourFormats.first { formats.contains(it) }
			depthFormat = depthFormats.first { formats.contains(it) }

			swapchains = XrUtil.createSwapchains(stack, session, viewConfigurationViews, session.manager.manager.getEnum(colourFormat).toLong(), session.manager.manager.getEnum(depthFormat).toLong())
		}

		for((i, swapchain) in swapchains.withIndex()) {
			session.manager.nameObject(swapchain.colour.address(), XR10.XR_OBJECT_TYPE_SWAPCHAIN, "OpenXR Colour Swapchain $i")
			session.manager.nameObject(swapchain.depth.address(), XR10.XR_OBJECT_TYPE_SWAPCHAIN, "OpenXR Depth Swapchain $i")
		}
	}

	override fun init() {
		shader.setRendererDefaults(uniforms)
	}

	fun render(){
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
				if(renderLayer(frameState.predictedDisplayTime(), pointer)){
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

	fun renderLayer(displayTime: Long, compositionLayerPtr: PointerBuffer): Boolean {
		MemoryStack.stackPush().use { stack ->
			val views = XrView.calloc(swapchains.size, stack)
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

			repeat(viewCount) { i -> renderView(views[i], swapchains[i], compositionLayerViews[i]) }

			compositionLayer.layerFlags((XR10.XR_COMPOSITION_LAYER_BLEND_TEXTURE_SOURCE_ALPHA_BIT or XR10.XR_COMPOSITION_LAYER_CORRECT_CHROMATIC_ABERRATION_BIT).toLong())
				.space(space)
				.views(compositionLayerViews)

			compositionLayerPtr[0] = compositionLayer.address()
			return true
		}
	}

	fun renderView(xrView: XrView, swapchain: OpenXrSwapchain, compositionLayerView: XrCompositionLayerProjectionView){

		val stack = MemoryStack.stackPush()
		val acquireInfo = XrSwapchainImageAcquireInfo.calloc(stack).`type$Default`()
		val b = stack.mallocInt(1)
		XrUtil.processResult(XR10.xrAcquireSwapchainImage(swapchain.colour, acquireInfo, b), "Failed to get swapchain colour image")
		val colourImage = b[0]
		XrUtil.processResult(XR10.xrAcquireSwapchainImage(swapchain.depth, acquireInfo, b), "Failed to get swapchain depth image")
		swapchain.framebuffer.setTextures(colourImage, b[0])

		val waitInfo = XrSwapchainImageWaitInfo.calloc(stack).`type$Default`()
			.timeout(XR10.XR_INFINITE_DURATION)
		XrUtil.processResult(XR10.xrWaitSwapchainImage(swapchain.colour, waitInfo), "Failed to wait for swapchain colour image")
		XrUtil.processResult(XR10.xrWaitSwapchainImage(swapchain.depth, waitInfo), "Failed to wait for swapchain depth image")

		viewportSize(swapchain.view.recommendedImageRectWidth(), swapchain.view.recommendedImageRectHeight())

		compositionLayerView.`type$Default`()
			.pose(xrView.pose())
			.fov(xrView.fov())
			.subImage { subimage ->
				subimage.swapchain(swapchain.colour)
					.imageRect { rect -> rect.set(0, 0, viewportSize.x, viewportSize.y) }
					.imageArrayIndex(0)
			}

		if (!api.beginPresentation()) return
		api.bindFramebuffer(swapchain.framebuffer, Vec4(.8f, .2f, .8f, 1f), -1f)

		updateView(xrView)

		shader.setUp(uniforms, this)

		for(i in 0..99){
			shader.setMat4("model", Mat4().scale(.15f).translate(-4.5f + (i % 10), -4.5f + (i / 10), 0f))
			shader.setVec4("colour", Vec4((i % 10) * .1f, (i / 10) * .1f, 0f, 1f))
			shader.draw("vertexBuffer", cubeMesh, this)
		}

		// Draw Hands
		shader.setVec4("colour", Vec4(0f, 0f, 1f, 1f))

		shader.setMat4("model", I.translateAssign(viewOffset) * session.manager.actionSet.palmPoseAction.getValue(session.leftHandPath).scale(.1f))
		shader.draw("vertexBuffer", cubeMesh, this)

		shader.setMat4("model", I.translateAssign(viewOffset) * session.manager.actionSet.palmPoseAction.getValue(session.rightHandPath).scale(.1f))
		shader.draw("vertexBuffer", cubeMesh, this)

		api.endFramebuffer(swapchain.framebuffer)
//		swapchain.framebuffer.colour.savePNG("debug")
		api.setViewport(Viewport(Vec2i(0), session.manager.window.framebufferSize))
		api.copyFramebuffer(swapchain.framebuffer, session.manager.window, this)
		api.present()

		val releaseInfo = XrSwapchainImageReleaseInfo.calloc(stack).`type$Default`()
		XrUtil.processResult(XR10.xrReleaseSwapchainImage(swapchain.colour, releaseInfo), "Failed to release swapchain colour image")
		XrUtil.processResult(XR10.xrReleaseSwapchainImage(swapchain.depth, releaseInfo), "Failed to release swapchain depth image")

		MemoryStack.stackPop()
	}

	fun updateView(view: XrView) {
		view.pose().`position$`().glm().plus(viewOffset, viewPos)
		XrUtil.createProjection(view.fov(), 100f, .05f, session.manager.manager.invertY(), projection)
		(I.translateAssign(viewOffset) * view.pose().mat4()).rigidInverse(this.view)
	}

	override fun getRenderingApi(): RenderingApi = api

	override fun delete() {
		api.delete()
		swapchains.delete()
	}

	companion object {
		// https://openxr-tutorial.com//linux/opengl/3-graphics.html#create-the-swapchains
		val depthFormats = listOf(TextureFormat.DEPTH32F, TextureFormat.DEPTH32, TextureFormat.DEPTH24, TextureFormat.DEPTH16)

		val blendModes = listOf(XR10.XR_ENVIRONMENT_BLEND_MODE_OPAQUE)

		val shader by lazy { ShaderLoader.loadRenderShader(ResourceKey("3d/colour")) }
		val uniforms by lazy { shader.compileUniforms() }
		val cubeMesh by lazy { Mesh.centerCubeShape }
	}

	/*

			Projection

			0.9172858 0.0 -0.17407216 0.0
			0.0 0.833537 -0.10614098 0.0
			0.0 0.0 -1.0010005 -0.10005003
			0.0 0.0 -1.0 0.0

			View

			0.9957638 0.026063077 0.08817748 -0.0019347556
			-0.026545785 0.9996383 0.004305865 0.26591817
			-0.08803337 -0.006628365 0.9960955 0.17437588
			0.0 0.0 0.0 1.0
	 */
}