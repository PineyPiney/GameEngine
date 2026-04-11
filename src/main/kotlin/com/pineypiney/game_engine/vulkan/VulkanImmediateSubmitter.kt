package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13

class VulkanImmediateSubmitter(val vulkan: VulkanManager) : Deletable {

	val immediateCommands = PoolAndBuffer.create(vulkan.device)
	val immediateFence = VkUtil.createFence(vulkan.device, VK10.VK_FENCE_CREATE_SIGNALED_BIT)

	fun submitImmediate(func: (cmd: PoolAndBuffer) -> Unit) {
		immediateFence.reset()
		immediateCommands.immediateSubmit(func)
		val cmdInfo = VkStructs.createBufferSubmits(immediateCommands.buffer, 0)
		val submitInfo = VkStructs.createSubmitInfo2s(cmdInfo, null, null)
		VK13.vkQueueSubmit2(vulkan.queue, submitInfo, immediateFence.handle)
		immediateFence.wait(1e9)
	}

	override fun delete() {
		immediateCommands.delete()
		immediateFence.delete()
	}
}