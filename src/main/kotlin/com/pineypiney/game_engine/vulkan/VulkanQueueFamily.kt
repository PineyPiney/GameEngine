package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkQueueFamilyProperties

class VulkanQueueFamily(val obj: VkQueueFamilyProperties, val i: Int, val gpu: VulkanPhysicalDevice) : Deletable {


	fun supportsGraphics() = (obj.queueFlags() and VK10.VK_QUEUE_GRAPHICS_BIT) == VK10.VK_QUEUE_GRAPHICS_BIT
	fun supportsCompute() = (obj.queueFlags() and VK10.VK_QUEUE_COMPUTE_BIT) == VK10.VK_QUEUE_COMPUTE_BIT
	fun supportsTransfer() = (obj.queueFlags() and VK10.VK_QUEUE_TRANSFER_BIT) == VK10.VK_QUEUE_TRANSFER_BIT
	fun supportsSparseBinding() = (obj.queueFlags() and VK10.VK_QUEUE_SPARSE_BINDING_BIT) == VK10.VK_QUEUE_SPARSE_BINDING_BIT

	fun supportsPresentation(surface: VulkanSurface): Boolean {
		val canPresent = MemoryUtil.memAllocInt(1)
		VkUtil.processError(KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(gpu.physicalDevice, i, surface.handle, canPresent), "Failed to get physical device's surface support")
		return canPresent[0] == VK10.VK_TRUE
	}

	override fun delete() {
		obj.free()
	}
}