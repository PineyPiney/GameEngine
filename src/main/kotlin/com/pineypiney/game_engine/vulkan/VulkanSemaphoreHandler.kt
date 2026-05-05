package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import kool.free
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import java.nio.LongBuffer

class VulkanSemaphoreHandler(val device: VulkanDevice, val buffer: LongBuffer, val flags: Int) : Deletable {

	val handle get() = buffer[0]

	fun recreate() {
		VK10.vkDestroySemaphore(device.device, handle, null)
		MemoryStack.stackPush().use { stack ->
			val createInfo = VkStructs.createSemaphoreInfo(stack, flags)
			VkUtil.processError(VK10.vkCreateSemaphore(device.device, createInfo, null, buffer), "Failed to recreate Vulkan Semaphore")
		}
	}

	override fun delete() {
		VK10.vkDestroySemaphore(device.device, handle, null)
		buffer.free()
	}
}