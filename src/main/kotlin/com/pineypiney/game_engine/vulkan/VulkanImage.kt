package com.pineypiney.game_engine.vulkan

import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13

class VulkanImage(override val device: VulkanDevice, override val image: Long, override val imageView: Long, override val format: Int, val allocation: Long, val size: Vec2i) : VulkanImageI {

	override val extents: Vec3i = Vec3i(size, 1)
	override var layout: Int = VK10.VK_IMAGE_LAYOUT_UNDEFINED

	override fun delete() {
		VK13.vkDestroyImageView(device.device, imageView, null)
		Vma.vmaDestroyImage(device.allocator, image, allocation)
	}
}