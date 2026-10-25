package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.XR10
import org.lwjgl.openxr.XrSwapchain
import org.lwjgl.openxr.XrViewConfigurationView

class OpenXrSwapchain(val view: XrViewConfigurationView, val colour: XrSwapchain, val depth: XrSwapchain, val framebuffer: OpenXrFramebuffer): Deletable {

	override fun delete() {
		framebuffer.delete()
		view.free()
		XrUtil.processResult(XR10.xrDestroySwapchain(colour), "Could not destroy Colour OpenXR Swapchain")
		XrUtil.processResult(XR10.xrDestroySwapchain(depth), "Could not destroy Depth OpenXR Swapchain")
	}
}