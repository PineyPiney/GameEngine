package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK10
import java.nio.ByteBuffer

abstract class VulkanDescriptorBinding(val binding: Int, val type: Int) : Deletable {

	abstract fun bind(writer: VulkanDescriptorWriter)

	abstract fun contains(uniform: String): Boolean

	abstract class Image(binding: Int, type: Int, val name: String) : VulkanDescriptorBinding(binding, type) {

		protected var image: VulkanImage? = null
		protected var layout: Int = 0
		protected var sampler = 0L

		fun setImage(image: VulkanImage, layout: Int = image.layout, sampler: Long = image.getSampler()) {
			this.image = image
			this.layout = layout
			this.sampler = sampler
		}

		override fun contains(uniform: String): Boolean = uniform == name

		override fun delete() {}
	}

	class StorageImage(binding: Int, name: String) : Image(binding, VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, name) {
		override fun bind(writer: VulkanDescriptorWriter) {
			image?.let { writer.writeStorageImage(binding, it, layout, sampler) }
		}
	}

	class CombinedSampler(binding: Int, name: String) : Image(binding, VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, name) {
		override fun bind(writer: VulkanDescriptorWriter) {
			image?.let { writer.writeCombinedImageSampler(binding, it, layout, sampler) }
		}
	}

	abstract class Buffer(val device: VulkanDevice, binding: Int, bufferUsage: Int, type: Int, val size: Long, val offsets: Map<String, Int>) : VulkanDescriptorBinding(binding, type) {

		val buffer = VmaBuffer.create(device, size, bufferUsage, Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)

		override fun contains(uniform: String): Boolean = offsets.containsKey(uniform)

		fun set(name: String, data: ByteBuffer, offset: Int, length: Int) {
			val index = offsets[name] ?: return
			val dst = this.buffer.getBuffer(size.toInt())
			dst.put(index, data, offset, length)
		}

		fun set(name: String, data: ByteBuffer) {
			val index = offsets[name] ?: return
			val dst = this.buffer.getBuffer(size.toInt())
			dst.put(index, data, 0, data.capacity())
		}

		fun get(name: String): ByteBuffer? {
			val index = offsets[name] ?: return null
			val dst = this.buffer.getBuffer(index.toLong(), size.toInt() - index)
			return dst
		}

		override fun delete() {
			buffer.delete()
		}
	}

	class UniformBuffer(device: VulkanDevice, binding: Int, size: Int, offsets: Map<String, Int>) :
		Buffer(device, binding, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, size.toLong(), offsets) {
		override fun bind(writer: VulkanDescriptorWriter) {
			writer.writeUniformBuffer(binding, buffer, size)
		}
	}
}