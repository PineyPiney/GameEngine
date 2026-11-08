package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import com.pineypiney.game_engine.vulkan.VulkanImmediateSubmitter
import glm_.has
import glm_.vec2.Vec2i
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK12
import org.lwjgl.vulkan.VK13

class VulkanCDSFramebuffer(
	override val device: VulkanDevice,
	size: Vec2i,
	val colourFormat: TextureFormat,
	val depthStencilFormat: TextureFormat,
	val colourUsage: Int
) : VulkanFramebuffer {

	override var size: Vec2i = size; private set

	override lateinit var colour: VulkanImage2D
	override lateinit var depthStencil: VulkanImage2D

	override fun init() {
		generateImages()
	}

	override fun setSize(size: Vec2i) {
		if (size != this.size) {
			this.size = size
			refreshImages()
		}
	}

	override fun setSize(width: Int, height: Int) {
		setSize(Vec2i(width, height))
	}

	fun refreshImages() {
		colour.delete()
		depthStencil.delete()
		generateImages()
	}

	fun generateImages() {
		val colourUsage = VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or colourUsage
		val depthUsage = VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT or VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT

		colour = VkUtil.createImage(device, "Framebuffer Colour Image", colourFormat, colourUsage, VK10.VK_IMAGE_ASPECT_COLOR_BIT, size)
		depthStencil = VkUtil.createImage(device, "Framebuffer Depth Image", depthStencilFormat, depthUsage, VK10.VK_IMAGE_ASPECT_DEPTH_BIT or VK10.VK_IMAGE_ASPECT_STENCIL_BIT, size)

		VulkanImmediateSubmitter.submitImmediate(device) { cmd ->
			depthStencil.transition(
				cmd,
				VK12.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
				VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT,
				VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
				VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT,
				VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT,
				false
			)
		}
	}

	override fun begin(cmd: PoolAndBuffer) {
		if (colourUsage has VK10.VK_IMAGE_USAGE_SAMPLED_BIT) colour.transition(
			cmd,
			VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
			VK13.VK_ACCESS_2_SHADER_SAMPLED_READ_BIT,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
			VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT,
			VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
			false
		)
		else colour.transition(
			cmd,
			VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
			VK13.VK_ACCESS_2_TRANSFER_READ_BIT,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
			VK13.VK_PIPELINE_STAGE_2_TRANSFER_BIT,
			VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
			false
		)
	}

	override fun end(cmd: PoolAndBuffer) {
		if (colourUsage has VK10.VK_IMAGE_USAGE_SAMPLED_BIT) colour.transition(
			cmd,
			VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
			VK13.VK_ACCESS_2_SHADER_SAMPLED_READ_BIT,
			VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
			VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT
		)
		else colour.transition(
			cmd,
			VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
			VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
			VK13.VK_ACCESS_2_TRANSFER_READ_BIT,
			VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
			VK13.VK_PIPELINE_STAGE_2_TRANSFER_BIT
		)
//		depthStencil.transition(cmd, VK12.VK_IMAGE_LAYOUT_DEPTH_READ_ONLY_STENCIL_ATTACHMENT_OPTIMAL, VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT, VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT, VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT, VK13.VK_PIPELINE_STAGE_2_FRAGMENT_SHADER_BIT)
	}

	override fun delete() {
		colour.delete()
		depthStencil.delete()
	}
}