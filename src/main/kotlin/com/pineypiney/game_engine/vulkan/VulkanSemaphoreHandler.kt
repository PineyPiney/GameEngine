package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import kool.free
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import java.nio.LongBuffer

class VulkanSemaphoreHandler(val device: VulkanDevice, val buffer: LongBuffer, val name: String, val flags: Int) : Deletable {

	val handle get() = buffer[0]

	init {
		device.nameObject(buffer[0], VK10.VK_OBJECT_TYPE_SEMAPHORE, name)
	}

	fun recreate() {
		VK10.vkDestroySemaphore(device.device, handle, null)
		MemoryStack.stackPush().use { stack ->
			val createInfo = VkStructs.createSemaphoreInfo(stack, flags)
			VkUtil.processResult(VK10.vkCreateSemaphore(device.device, createInfo, null, buffer), "Failed to recreate Vulkan Semaphore")
			device.nameObject(buffer[0], VK10.VK_OBJECT_TYPE_SEMAPHORE, name)
		}
	}

	override fun delete() {
		VK10.vkDestroySemaphore(device.device, handle, null)
		buffer.free()
	}
}