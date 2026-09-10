package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.vulkan.VulkanRendering
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.window.Viewport
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10

class VulkanFrameObjects(device: VulkanDevice, viewport: () -> Viewport) : Deletable {

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

		// These are arbitrary values chosen in the following guide
		// https://vkguide.dev/docs/new_chapter_4/descriptor_abstractions/
		val ratios = mapOf(
			VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE to 3f,
			VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER to 3f,
			VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER to 3f,
			VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER to 4f,
		)
		frameDescriptorAllocator.init(1000, ratios)
	}

	val api = VulkanRendering(commands, frameDescriptorAllocator, viewport)
	val deletionQueue = DeletionQueue()

	fun refresh() {
		deletionQueue.flush()
		frameDescriptorAllocator.clearPools()
		swapchainSemaphore.recreate()
		renderSemaphore.recreate()
	}

	fun begin(flags: Int = VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT): PoolAndBuffer {
		commands.resetBuffer()
		commands.begin(flags)
		return commands
	}

	override fun delete() {
		renderFence.delete()
		swapchainSemaphore.delete()
		renderSemaphore.delete()
		commands.delete()
		frameDescriptorAllocator.delete()
		deletionQueue.flush()
	}
}