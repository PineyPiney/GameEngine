package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkQueue

class VulkanImmediateSubmitter(device: VulkanDevice, val queue: VkQueue = device.getQueue(0)) : Deletable {

	constructor(vulkan: VulkanManager) : this(vulkan.device, vulkan.queue)

	val immediateCommands: PoolAndBuffer
	val immediateFence: VulkanFence

	init {
		MemoryStack.stackPush().use { stack ->
			immediateCommands = PoolAndBuffer.create(device, stack, "Immediate")
			immediateFence = device.createFence(stack, VK10.VK_FENCE_CREATE_SIGNALED_BIT, "Immediate")
		}
	}

	fun submitImmediate(func: (cmd: PoolAndBuffer) -> Unit) {
		immediateFence.reset()
		immediateCommands.execute(func)
		MemoryStack.stackPush().use { stack ->
			val cmdInfo = VkStructs.createBufferSubmits(stack, immediateCommands.buffer, 0)
			val submitInfo = VkStructs.createSubmitInfo2s(stack, cmdInfo, null, null)
			VK13.vkQueueSubmit2(queue, submitInfo, immediateFence.handle)
		}
		immediateFence.wait(1e9)
	}

	fun <E> submitImmediate(func: (cmd: PoolAndBuffer) -> E): E {
		immediateFence.reset()
		val ret = immediateCommands.execute(func)
		MemoryStack.stackPush().use { stack ->
			val cmdInfo = VkStructs.createBufferSubmits(stack, immediateCommands.buffer, 0)
			val submitInfo = VkStructs.createSubmitInfo2s(stack, cmdInfo, null, null)
			VK13.vkQueueSubmit2(queue, submitInfo, immediateFence.handle)
		}
		immediateFence.wait(1e9)
		return ret
	}

	override fun delete() {
		immediateCommands.delete()
		immediateFence.delete()
	}

	companion object {
		fun <E> submitImmediate(device: VulkanDevice, func: (cmd: PoolAndBuffer) -> E): E {
			val submitter = VulkanImmediateSubmitter(device)
			val ret = submitter.submitImmediate(func)
			submitter.delete()
			return ret
		}

		fun <E : Deletable, R> submitAndFetch(device: VulkanDevice, get: (E) -> R, func: (cmd: PoolAndBuffer) -> E): R {
			val submitter = VulkanImmediateSubmitter(device)
			val e = submitter.submitImmediate(func)
			val ret = get(e)
			e.delete()
			submitter.delete()
			return ret
		}
	}
}