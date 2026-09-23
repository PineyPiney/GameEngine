package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VulkanDevice
import glm_.vec2.Vec2i
import org.lwjgl.vulkan.VK10

class VulkanFramebuffer(
	val device: VulkanDevice,
	size: Vec2i,
	val colourFormat: TextureFormat,
	val depthStencilFormat: TextureFormat,
	val colourUsage: Int
) : Framebuffer {

	override var size: Vec2i = size; private set

	override lateinit var colour: VulkanImage2D
	override lateinit var depthStencil: VulkanImage2D

	override fun init() {
		generateImages()
	}

	override fun setSize(size: Vec2i) {
		this.size = size
		refreshImages()
	}

	override fun setSize(width: Int, height: Int) {
		size = Vec2i(width, height)
		refreshImages()
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

	override fun delete() {
		colour.delete()
		depthStencil.delete()
	}
}