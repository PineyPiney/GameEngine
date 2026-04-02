package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import kool.free
import org.lwjgl.vulkan.VK10
import java.nio.LongBuffer

class VulkanSemaphoreHandler(val device: VulkanDevice, val buffer: LongBuffer) : Deletable {
	val handle get() = buffer[0]

	override fun delete() {
		VK10.vkDestroySemaphore(device.device, handle, null)
		buffer.free()
	}
}