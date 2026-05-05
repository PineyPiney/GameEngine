package com.pineypiney.game_engine.resources.textures.vulkan

import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.vulkan.VK13

class VulkanSwapchainImage(device: VulkanDevice, image: Long, imageView: Long, format: TextureFormat, width: Int, height: Int) :
	VulkanImage2D(device, "Swapchain Image", image, imageView, format, 0L, width, height) {

	override fun delete() {
		// The images are destroyed by the swapchain, only the views need to be destroyed
		VK13.vkDestroyImageView(device.device, imageView, null)
	}
}