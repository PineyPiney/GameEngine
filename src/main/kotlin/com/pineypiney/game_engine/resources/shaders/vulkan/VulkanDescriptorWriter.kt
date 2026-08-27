package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDescriptorBufferInfo
import org.lwjgl.vulkan.VkDescriptorImageInfo
import org.lwjgl.vulkan.VkWriteDescriptorSet

class VulkanDescriptorWriter {

	val imageInfos = mutableListOf<VkDescriptorImageInfo>()
	val bufferInfos = mutableListOf<VkDescriptorBufferInfo>()
	val writes = mutableListOf<VkWriteDescriptorSet>()

	fun writeImage(binding: Int, imageView: Long, layout: Int, sampler: Long, type: Int): VulkanDescriptorWriter {

		val info = VkDescriptorImageInfo.calloc(1)
			.imageView(imageView)
			.sampler(sampler)
			.imageLayout(layout)
		imageInfos.add(info[0])

		val write = VkWriteDescriptorSet.calloc()
			.`sType$Default`()
			.dstBinding(binding)
			.dstSet(0L)
			.descriptorCount(1)
			.descriptorType(type)
			.pImageInfo(info)
		writes.add(write)
		return this
	}

	fun writeStorageImage(binding: Int, image: VulkanImage, layout: Int = image.layout, sampler: Long = image.getSampler()): VulkanDescriptorWriter {
		return writeImage(binding, image.imageView, layout, sampler, VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
	}

	fun writeCombinedImageSampler(binding: Int, image: VulkanImage, layout: Int = image.layout, sampler: Long = image.getSampler()): VulkanDescriptorWriter {
		image.parameters.minFilter.vulkanImage
		return writeImage(binding, image.imageView, layout, sampler, VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
	}

	fun writeBuffer(binding: Int, buffer: Long, size: Long, offset: Long, type: Int): VulkanDescriptorWriter {

		val info = VkDescriptorBufferInfo.calloc(1)
			.buffer(buffer)
			.range(size)
			.offset(offset)
		bufferInfos.add(info[0])

		val write = VkWriteDescriptorSet.calloc()
			.`sType$Default`()
			.dstBinding(binding)
			.dstSet(0L)
			.descriptorCount(1)
			.descriptorType(type)
			.pBufferInfo(info)
		writes.add(write)
		return this
	}

	fun writeUniformBuffer(binding: Int, buffer: VmaBuffer, size: Long, offset: Long = 0): VulkanDescriptorWriter {
		return writeBuffer(binding, buffer.buffer, size, offset, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
	}

	fun clear(): VulkanDescriptorWriter {
		imageInfos.clear()
		bufferInfos.clear()
		writes.clear()
		return this
	}

	fun updateSet(device: VulkanDevice, set: VulkanDescriptorSet): VulkanDescriptorWriter {
		val writesBuffer = VkWriteDescriptorSet.calloc(writes.size)
		for (write in writes) {
			writesBuffer.put(write.dstSet(set.handle))
		}
		VK10.vkUpdateDescriptorSets(device.device, writesBuffer.flip(), null)
		writesBuffer.free()
		return this
	}
}