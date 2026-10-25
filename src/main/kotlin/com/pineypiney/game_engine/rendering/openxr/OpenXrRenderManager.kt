package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.xr.*
import kool.set
import org.lwjgl.PointerBuffer
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

open class OpenXrRenderManager(val session: OpenXrSession, blendModes: List<Int>) : Initialisable {

	val deletionQueue = OpenXrDeletionQueue()

	val instance get() = session.manager.instance
	val systemID get() = session.manager.systemID
	val space get() = session.space

	val viewConfiguration: Int
	val blendMode: Int

	val colourFormat: TextureFormat
	val depthFormat: TextureFormat
	val swapchainFormats: List<Long>
	val swapchains: List<OpenXrSwapchain>

	var viewIndex = 0
	val swapchain get() = swapchains[viewIndex]

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
			deletionQueue.pushAll(swapchains)
		}

		for((i, swapchain) in swapchains.withIndex()) {
			session.manager.nameObject(swapchain.colour.address(), XR10.XR_OBJECT_TYPE_SWAPCHAIN, "OpenXR Colour Swapchain $i")
			session.manager.nameObject(swapchain.depth.address(), XR10.XR_OBJECT_TYPE_SWAPCHAIN, "OpenXR Depth Swapchain $i")
		}
	}

	override fun init() {}

	fun <E: OpenXrGameLogicI> render(game: E, tickDelta: Double){
		MemoryStack.stackPush().use { stack ->
			val frameState = XrFrameState.calloc(stack).`type$Default`()
			val waitInfo = XrFrameWaitInfo.calloc(stack).`type$Default`()
			XrUtil.processResult(XR10.xrWaitFrame(session.session, waitInfo, frameState), "Failed to wait for frame")

			val beginInfo = XrFrameBeginInfo.calloc(stack).`type$Default`()
			XrUtil.processResult(XR10.xrBeginFrame(session.session, beginInfo), "Failed to begin frame")

			val layerPointers = mutableListOf<Long>()

			if(session.active && frameState.shouldRender()){
				session.pollActions(frameState.predictedDisplayTime())
				game.processActions(session, frameState.predictedDisplayTime())
				val pointer = stack.mallocPointer(1)
				if(renderLayer(game, tickDelta, frameState.predictedDisplayTime(), pointer)){
					layerPointers.add(pointer[0])
				}
				addLayers(layerPointers)
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

	open fun addLayers(layers: MutableList<Long>){

	}

	fun <E: OpenXrGameLogicI> renderLayer(game: E, tickDelta: Double, displayTime: Long, compositionLayerPtr: PointerBuffer): Boolean {
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

			repeat(viewCount) { i ->
				viewIndex = i
				renderView(game, tickDelta, views[i], swapchains[i], compositionLayerViews[i])
			}

			compositionLayer.layerFlags((XR10.XR_COMPOSITION_LAYER_BLEND_TEXTURE_SOURCE_ALPHA_BIT or XR10.XR_COMPOSITION_LAYER_CORRECT_CHROMATIC_ABERRATION_BIT).toLong())
				.space(space)
				.views(compositionLayerViews)

			compositionLayerPtr[0] = compositionLayer.address()
			return true
		}
	}

	fun <E: OpenXrGameLogicI> renderView(game: E, tickDelta: Double, xrView: XrView, swapchain: OpenXrSwapchain, compositionLayerView: XrCompositionLayerProjectionView) {

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

		val renderer = game.renderer
		renderer.viewportSize(swapchain.view.recommendedImageRectWidth(), swapchain.view.recommendedImageRectHeight())

		val imageRect = XrRect2Di.calloc(stack).set(0, 0, renderer.viewportSize.x, renderer.viewportSize.y)

		val depthInfo = XrCompositionLayerDepthInfoKHR.calloc(stack).`type$Default`()
			.nearZ(100f).farZ(.05f)
			.minDepth(-1f).maxDepth(1f)
			.subImage { subimage ->
				subimage.swapchain(swapchain.depth)
					.imageRect(imageRect)
			}

		compositionLayerView.`type$Default`()
			.pose(xrView.pose())
			.fov(xrView.fov())
			.subImage { subimage ->
				subimage.swapchain(swapchain.colour)
					.imageRect(imageRect)
					.imageArrayIndex(0)
			}
			.next(depthInfo)

		game.renderer.updateView(xrView)

		game.render(tickDelta)

		val releaseInfo = XrSwapchainImageReleaseInfo.calloc(stack).`type$Default`()
		XrUtil.processResult(XR10.xrReleaseSwapchainImage(swapchain.colour, releaseInfo), "Failed to release swapchain colour image")
		XrUtil.processResult(XR10.xrReleaseSwapchainImage(swapchain.depth, releaseInfo), "Failed to release swapchain depth image")

		MemoryStack.stackPop()
	}

	override fun delete() {
		deletionQueue.flush()
	}

	companion object {
		// https://openxr-tutorial.com//linux/opengl/3-graphics.html#create-the-swapchains
		val depthFormats = listOf(TextureFormat.DEPTH32F, TextureFormat.DEPTH32, TextureFormat.DEPTH24, TextureFormat.DEPTH16)
	}
}