package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK10
import java.nio.ByteBuffer

abstract class VulkanDescriptorBinding(val binding: Int, val type: Int, val name: String) : Deletable {

	abstract fun bind(writer: VulkanDescriptorWriter)

	abstract fun contains(uniform: String): Boolean

	abstract class Image(binding: Int, type: Int, name: String) : VulkanDescriptorBinding(binding, type, name) {

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

		override fun toString(): String {
			return "Image Binding('$name')"
		}
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

	abstract class Buffer(val device: VulkanDevice, binding: Int, bufferUsage: Int, type: Int, name: String, val size: Long, val offsets: Map<String, Int>) :
		VulkanDescriptorBinding(binding, type, name) {

		val buffer = VmaBuffer.create(device, size, bufferUsage, Vma.VMA_MEMORY_USAGE_CPU_TO_GPU, "$name Descriptor Binding")

		override fun contains(uniform: String): Boolean = offsets.containsKey(getOffsetName(uniform))

		fun set(name: String, data: ByteBuffer, offset: Int, length: Int) {
			val index = offsets[getOffsetName(name) ?: return] ?: return
			val dst = this.buffer.getBuffer(size.toInt())
			dst.put(index, data, offset, length)
		}

		fun set(name: String, data: ByteBuffer) {
			val index = offsets[getOffsetName(name) ?: return] ?: return
			val dst = this.buffer.getBuffer(size.toInt())
			dst.put(index, data, 0, data.capacity())
		}

		fun get(name: String): ByteBuffer? {
			val index = offsets[getOffsetName(name) ?: return null] ?: return null
			val dst = this.buffer.getBuffer(index.toLong(), size.toInt() - index)
			return dst
		}

		fun getOffsetName(uniform: String): String? {
			return getOffsetName(name, uniform)
		}

		override fun delete() {
			buffer.delete()
		}

		override fun toString(): String {
			return "Buffer Binding('$name')"
		}
	}

	class UniformBuffer(device: VulkanDevice, binding: Int, name: String, size: Int, offsets: Map<String, Int>) :
		Buffer(device, binding, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, name, size.toLong(), offsets) {
		override fun bind(writer: VulkanDescriptorWriter) {
			writer.writeUniformBuffer(binding, buffer, size)
		}
	}

	companion object {


		fun getOffsetName(containerName: String, uniform: String): String? {
			val dot1 = uniform.indexOf('.')
			// If there is no dot separator then this must be an unnamed buffer
			if (dot1 == -1) return if (containerName.isEmpty()) uniform else null

			val first = uniform.substring(0, dot1)

			// If name is empty then the first part must be for a variable
			if (containerName.isEmpty()) return first
			// Otherwise the first part must be for this variable's name
			else if (containerName != first) return null

			val dot2 = uniform.indexOf('.', dot1 + 1)

			// The next section must match one of this struct's variables
			return if (dot2 == -1) uniform.substring(dot1 + 1)
			else uniform.substring(dot1 + 1, dot2)
		}
	}
}