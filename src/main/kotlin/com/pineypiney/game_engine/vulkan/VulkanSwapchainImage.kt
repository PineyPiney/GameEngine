package com.pineypiney.game_engine.vulkan

import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13

class VulkanSwapchainImage(override val device: VulkanDevice, override val image: Long, override val imageView: Long, override val format: Int, val size: Vec2i) : VulkanImageI {

	override val extents: Vec3i = Vec3i(size, 1)
	override var layout: Int = VK10.VK_IMAGE_LAYOUT_UNDEFINED

	override fun delete() {
		// The images are destroyed by the swapchain, only the views need to be destroyed
		VK13.vkDestroyImageView(device.device, imageView, null)
	}
}