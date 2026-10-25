package com.pineypiney.game_engine_test.xr

import com.pineypiney.game_engine.rendering.openxr.OpenXrRenderManager
import com.pineypiney.game_engine.xr.OpenXrSession
import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack

class TestRenderManager(session: OpenXrSession, blendModes: List<Int>): OpenXrRenderManager(session, blendModes) {

	lateinit var passthrough: XrPassthroughFB
	lateinit var passthroughLayer: XrPassthroughLayerFB
	lateinit var compLayer: XrCompositionLayerPassthroughFB

	override fun init() {
		if(TestOpenXrEngine.supportsPassthrough){
			MemoryStack.stackPush().use { stack ->
				val b = stack.mallocPointer(1)

				val createInfo = XrPassthroughCreateInfoFB.calloc(stack).`type$Default`()
					.flags(FBPassthrough.XR_PASSTHROUGH_IS_RUNNING_AT_CREATION_BIT_FB.toLong())

				XrUtil.processResult(FBPassthrough.xrCreatePassthroughFB(session.session, createInfo, b), "Failed to create FB Passthrough")
				passthrough = XrPassthroughFB(b[0], session.session)

				val layerCreateInfo = XrPassthroughLayerCreateInfoFB.calloc(stack).`type$Default`()
					.passthrough(passthrough)
					.purpose(FBPassthrough.XR_PASSTHROUGH_LAYER_PURPOSE_RECONSTRUCTION_FB)
					.flags(createInfo.flags())

				FBPassthrough.xrCreatePassthroughLayerFB(session.session, layerCreateInfo, b)
				passthroughLayer = XrPassthroughLayerFB(b[0], session.session)

				deletionQueue.pushHandle(passthrough, FBPassthrough::xrDestroyPassthroughFB)
				deletionQueue.pushHandle(passthroughLayer, FBPassthrough::xrDestroyPassthroughLayerFB)

				compLayer = XrCompositionLayerPassthroughFB.calloc().`type$Default`()
					.layerHandle(passthroughLayer)
					.flags(XR10.XR_COMPOSITION_LAYER_BLEND_TEXTURE_SOURCE_ALPHA_BIT.toLong())
			}
		}
	}

	override fun addLayers(layers: MutableList<Long>) {
		if(TestOpenXrEngine.supportsPassthrough){
			layers.add(compLayer.address())
		}
	}
}