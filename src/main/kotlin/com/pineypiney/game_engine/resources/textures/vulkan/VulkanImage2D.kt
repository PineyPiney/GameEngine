package com.pineypiney.game_engine.resources.textures.vulkan

import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage.Companion.getImageAspect
import com.pineypiney.game_engine.vulkan.VkUtil
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import com.pineypiney.game_engine.vulkan.VulkanImmediateSubmitter
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13
import java.nio.ByteBuffer

open class VulkanImage2D(
	override val device: VulkanDevice,
	override val id: String,
	override val image: Long,
	override val imageView: Long,
	override val format: TextureFormat,
	val allocation: Long,
	override val width: Int,
	override val height: Int,
	override val parameters: TextureParameters = TextureParameters()
) : VulkanImage, Texture2D {

	override var layout: Int = VK10.VK_IMAGE_LAYOUT_UNDEFINED
	override val depth: Int get() = 1

	override fun setData(data: ByteBuffer, format: TextureFormat) {
		VulkanImmediateSubmitter.submitImmediate(device) { cmd ->
			uploadData(cmd, format, data)
		}
	}

	override fun setSubData(data: ByteBuffer, x: Int, y: Int, width: Int, height: Int, format: TextureFormat) {
		VulkanImmediateSubmitter.submitImmediate(device) { cmd ->
			uploadData(cmd, x, y, 0, width, height, 1, format, data)
		}
	}

	override fun getSubData(x: Int, y: Int, width: Int, height: Int, format: TextureFormat): ByteBuffer {

		val data: ByteBuffer =
			if (this.format == format) VulkanImmediateSubmitter.submitAndFetch(device, VmaBuffer::getBuffer) { cmd -> fetchData(cmd, x, y, 0, width, height, 1, getImageAspect(format)) }
			else {
				val usage = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT or
						VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT
				val converted = VkUtil.createImage(device, id, format, usage, VK10.VK_IMAGE_ASPECT_COLOR_BIT, size)
				VulkanImmediateSubmitter.submitAndFetch(device, VmaBuffer::getBuffer) { cmd ->
					copyTo(cmd, converted)
					converted.fetchData(cmd, x, y, 0, width, height, 1, getImageAspect(format))
				}
			}
		return data
	}

	override fun delete() {
		VK13.vkDestroyImageView(device.device, imageView, null)
		Vma.vmaDestroyImage(device.allocator, image, allocation)
	}
}