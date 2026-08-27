package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.vulkan.VulkanRendering
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.util.DeletionQueue
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10

class VulkanFrameObjects(device: VulkanDevice, drawImage: () -> VulkanImage2D, depthImage: () -> VulkanImage2D) : Deletable {

	val commands: PoolAndBuffer
	val renderFence: VulkanFence
	val swapchainSemaphore: VulkanSemaphoreHandler
	val renderSemaphore: VulkanSemaphoreHandler
	val frameDescriptorAllocator = GrowableVulkanDescriptorAllocator(device)

	init {

		MemoryStack.stackPush().use { stack ->
			commands = PoolAndBuffer.create(device, stack, "Frame Objects")
			renderFence = device.createFence(stack, VK10.VK_FENCE_CREATE_SIGNALED_BIT, "Render")
			swapchainSemaphore = device.createSemaphore(stack, 0, "Swapchain")
			renderSemaphore = device.createSemaphore(stack, 0, "Render")
		}
		val ratios = mapOf(
			VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE to 3f,
			VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER to 3f,
			VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER to 3f,
			VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER to 4f,
		)
		frameDescriptorAllocator.init(1000, ratios)
	}

	val api = VulkanRendering(commands, frameDescriptorAllocator, drawImage, depthImage)
	val deletionQueue = DeletionQueue()

	override fun delete() {
		renderFence.delete()
		swapchainSemaphore.delete()
		renderSemaphore.delete()
		commands.delete()
		frameDescriptorAllocator.delete()
		deletionQueue.flush()
	}
}