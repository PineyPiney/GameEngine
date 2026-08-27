package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma
import org.lwjgl.util.vma.VmaAllocatorCreateInfo
import org.lwjgl.util.vma.VmaVulkanFunctions
import org.lwjgl.vulkan.*

class VulkanDevice(val device: VkDevice, val physicalDevice: VulkanPhysicalDevice, val queueFamilyIndex: Int) : Deletable {

	val allocator: Long

	val samplers = mutableMapOf<Int, Long>()

	init {
		MemoryStack.stackPush().use { stack ->
			val vulkanFunctions = VmaVulkanFunctions.calloc(stack).set(device.physicalDevice.instance, device)

			val allocatorInfo = VmaAllocatorCreateInfo.calloc(stack)
				.device(device)
				.physicalDevice(device.physicalDevice)
				.instance(device.physicalDevice.instance)
				.pVulkanFunctions(vulkanFunctions)
				.flags(Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT)

			val buffer = stack.mallocPointer(1)
			VkUtil.processResult(Vma.vmaCreateAllocator(allocatorInfo, buffer), "Failed to create Vulkan Memory Allocator")
			allocator = buffer[0]
		}
	}

	fun getBufferAddress(stack: MemoryStack, buffer: VmaBuffer): Long {
		val deviceAddressInfo = VkBufferDeviceAddressInfo.calloc(stack)
			.`sType$Default`()
			.buffer(buffer.buffer)
		return VK12.vkGetBufferDeviceAddress(device, deviceAddressInfo)
	}

	fun getQueue(index: Int): VkQueue {
		MemoryStack.stackPush().use { stack ->
			val pointer = stack.mallocPointer(1)
			VK10.vkGetDeviceQueue(device, queueFamilyIndex, index, pointer)
			nameObject(pointer[0], VK10.VK_OBJECT_TYPE_QUEUE, "Device Queue($index)")
			return VkQueue(pointer[0], device)
		}
	}

	fun createCommandPool(stack: MemoryStack, name: String, familyIndex: Int = queueFamilyIndex, flags: Int = VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT): Long {
		val createInfo = VkStructs.createCommandPoolInfo(stack, familyIndex, flags)
		val handle = VkUtil.getLong("Command Pool", this, stack, createInfo, VK10::vkCreateCommandPool)
		nameObject(handle, VK10.VK_OBJECT_TYPE_COMMAND_POOL, name)
		return handle
	}

	fun createCommandBuffer(stack: MemoryStack, commandPool: Long, name: String): VkCommandBuffer {
		val allocateInfo = VkStructs.createCommandBufferInfo(stack, commandPool)
		val handle = VkUtil.allocatePointer("", this, stack, allocateInfo, VK10::vkAllocateCommandBuffers)
		nameObject(handle, VK10.VK_OBJECT_TYPE_COMMAND_BUFFER, name)
		return VkCommandBuffer(handle, device)
	}

	fun createFence(stack: MemoryStack, flags: Int, name: String): VulkanFence {
		val fenceInfo = VkStructs.createFenceInfo(stack, flags)
		val handle = VkUtil.getLong("Vulkan Fence", this, stack, fenceInfo, VK10::vkCreateFence)
		nameObject(handle, VK10.VK_OBJECT_TYPE_FENCE, name)
		return VulkanFence(this, handle)
	}

	fun createSemaphore(stack: MemoryStack, flags: Int, name: String): VulkanSemaphoreHandler {
		val semaphoreInfo = VkStructs.createSemaphoreInfo(stack, flags)
		val buffer = MemoryUtil.memAllocLong(1)
		VK10.vkCreateSemaphore(device, semaphoreInfo, null, buffer)
		val handler = VulkanSemaphoreHandler(this, buffer, name, flags)
		return handler
	}

	fun createSampler(stack: MemoryStack, magFilter: Int, minFilter: Int): Long {
		val createInfo = VkStructs.createSamplerInfo(stack, magFilter, minFilter)
		return VkUtil.getLong("Sampler", this, stack, createInfo, VK10::vkCreateSampler)
	}

	fun nameObject(ptr: Long, type: Int, name: String) {
		val name = MemoryUtil.memUTF8(name)
		MemoryStack.stackPush().use { stack ->
			val info = VkDebugUtilsObjectNameInfoEXT.calloc(stack).`sType$Default`()
				.objectHandle(ptr)
				.objectType(type)
				.pObjectName(name)
			EXTDebugUtils.vkSetDebugUtilsObjectNameEXT(device, info)
		}
	}

	fun waitIdle() = VK10.vkDeviceWaitIdle(device)

	override fun delete() {
		samplers.forEach { (_, value) ->
			VK10.vkDestroySampler(device, value, null)
		}
		Vma.vmaDestroyAllocator(allocator)
		VK10.vkDestroyDevice(device, null)
	}

	fun getOrCreateSampler(params: TextureParameters): Long {
		val min = params.minFilter.vulkanImage
		val mag = params.magFilter.vulkanImage
		val key = (min shl 2) or mag
		MemoryStack.stackPush().use { stack ->
			return samplers.getOrPut(key) { createSampler(stack, mag, min) }
		}
	}
}