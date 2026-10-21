package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import glm_.vec2.Vec2i
import org.lwjgl.vulkan.VK10

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
	}

	override fun begin(cmd: PoolAndBuffer) {
		colour.transition(cmd, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, false)
		depthStencil.transition(cmd, VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, false)
	}

	override fun end(cmd: PoolAndBuffer) {
		if (colourUsage and VK10.VK_IMAGE_USAGE_SAMPLED_BIT > 0) colour.transition(cmd, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
	}

	override fun delete() {
		colour.delete()
		depthStencil.delete()
	}
}