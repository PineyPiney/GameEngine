package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.rendering.vulkan.VulkanFramebuffer
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanSwapchainImage
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import glm_.vec2.Vec2i
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK12

class OpenXrVkFramebuffer(override val device: VulkanDevice, val colourTextures: List<VulkanSwapchainImage>, val depthTextures: List<VulkanSwapchainImage>) : VulkanFramebuffer, OpenXrFramebuffer{

	override var colourIndex = 0
	override var depthIndex = 0

	override val colour: VulkanSwapchainImage get() = colourTextures[colourIndex]
	override val depthStencil: VulkanSwapchainImage get() = depthTextures[depthIndex]
	override val size: Vec2i get() = colour.size

	override fun init() {

	}

	override fun setTextures(colour: Int, depth: Int){
		colourIndex = colour
		depthIndex = depth
	}

	override fun begin(cmd: PoolAndBuffer) {
		colour.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false)
		depthStencil.transition(cmd, VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL, false)
	}

	override fun end(cmd: PoolAndBuffer) {
		colour.transition(cmd, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
	}

	override fun delete() {
		colourTextures.delete()
		depthTextures.delete()
	}
}