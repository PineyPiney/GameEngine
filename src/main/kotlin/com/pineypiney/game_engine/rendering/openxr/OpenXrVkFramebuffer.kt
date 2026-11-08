package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.rendering.vulkan.VulkanFramebuffer
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanSwapchainImage
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import glm_.vec2.Vec2i
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK12
import org.lwjgl.vulkan.VK13

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
		colour.transition(
			cmd,
			VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
			VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
			VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT,
			false
		)
		depthStencil.transition(
			cmd,
			VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL,
			VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT,
			VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
			VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT,
			VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT,
			false
		)
	}

	override fun end(cmd: PoolAndBuffer) {
		colour.transition(
			cmd,
			VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT,
			VK13.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT,
			VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
		)
	}

	override fun delete() {
		colourTextures.delete()
		depthTextures.delete()
	}
}