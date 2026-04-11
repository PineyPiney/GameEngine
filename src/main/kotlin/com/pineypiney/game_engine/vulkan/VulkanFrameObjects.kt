package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.meshes.VulkanRendering
import org.lwjgl.vulkan.VK10

class VulkanFrameObjects(device: VulkanDevice) : Deletable {

	val commands = PoolAndBuffer.create(device)
	val renderFence = VkUtil.createFence(device, VK10.VK_FENCE_CREATE_SIGNALED_BIT)
	val swapchainSemaphore = VkUtil.createSemaphore(device, 0)
	val renderSemaphore = VkUtil.createSemaphore(device, 0)
	val api = VulkanRendering(commands)

	override fun delete() {
		renderFence.delete()
		swapchainSemaphore.delete()
		renderSemaphore.delete()
		commands.delete()
	}
}