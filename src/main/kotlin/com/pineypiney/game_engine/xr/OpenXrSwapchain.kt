package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.openxr.XR10
import org.lwjgl.openxr.XrSwapchain
import org.lwjgl.openxr.XrViewConfigurationView

class OpenXrSwapchain(val view: XrViewConfigurationView, val colour: XrSwapchain, val depth: XrSwapchain, val framebuffer: OpenXrGlFramebuffer): Deletable {

	override fun delete() {
		framebuffer.delete()

		XrUtil.processResult(XR10.xrDestroySwapchain(colour), "Could not destroy OpenXR Swapchain")
		XrUtil.processResult(XR10.xrDestroySwapchain(depth), "Could not destroy OpenXR Swapchain")
	}
}