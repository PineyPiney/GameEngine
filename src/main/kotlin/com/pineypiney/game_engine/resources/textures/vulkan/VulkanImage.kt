package com.pineypiney.game_engine.resources.textures.vulkan

import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.vulkan.*
import glm_.vec3.Vec3i
import kool.free
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.*
import java.nio.ByteBuffer

interface VulkanImage : Texture {

	val device: VulkanDevice
	val image: Long
	val imageView: Long
	var layout: Int
	val parameters: TextureParameters

	val extents: Vec3i get() = Vec3i(width, height, depth)

	override fun getTextureBinding(): Int = 0

	override fun bind() {

	}

	override fun unbind() {

	}

	override fun clear() {

	}

	override fun setSamples(samples: Int, fixedSample: Boolean) {

	}

	override fun getData(format: TextureFormat): ByteBuffer {
		val submitter = VulkanImmediateSubmitter(device, device.getQueue(0))
		val data = fetchData(submitter)
		submitter.delete()
		return data
	}

	fun getMemoryRequirements(): VkMemoryRequirements {
		val requirements = VkMemoryRequirements.calloc()
		VK10.vkGetImageMemoryRequirements(device.device, image, requirements)
		return requirements
	}

	fun getMemoryAllocateInfo(propertyFlags: Int): VkMemoryAllocateInfo {
		val requirements = getMemoryRequirements()
		return VkMemoryAllocateInfo.calloc()
			.`sType$Default`()
			.allocationSize(requirements.size())
			.memoryTypeIndex(device.physicalDevice.getMemoryType(requirements.memoryTypeBits(), propertyFlags))
	}

	fun transition(cmd: PoolAndBuffer, newLayout: Int, keepData: Boolean = true) {
		val aspectMask = if (newLayout == VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL) VK10.VK_IMAGE_ASPECT_DEPTH_BIT else VK10.VK_IMAGE_ASPECT_COLOR_BIT

		MemoryStack.stackPush().use { stack ->
			val imageBarrier = VkImageMemoryBarrier2.calloc(1, stack)
				.`sType$Default`()
				.srcStageMask(VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
				.srcAccessMask(VK13.VK_ACCESS_2_MEMORY_WRITE_BIT)
				.dstStageMask(VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
				.dstAccessMask(VK13.VK_ACCESS_2_MEMORY_WRITE_BIT or VK13.VK_ACCESS_2_MEMORY_READ_BIT)
				.oldLayout(if (keepData) layout else VK10.VK_IMAGE_LAYOUT_UNDEFINED)
				.newLayout(newLayout)
				.image(image)
				.subresourceRange(VkStructs.createImageRange(stack, aspectMask))

			val dependInfo = VkDependencyInfo.calloc(stack)
				.`sType$Default`()
				.pImageMemoryBarriers(imageBarrier)

			VK13.vkCmdPipelineBarrier2(cmd.buffer, dependInfo)
			layout = newLayout
		}
	}

	fun copyTo(cmd: PoolAndBuffer, dst: VulkanImage, srcOff: Vec3i = Vec3i(0), srcSize: Vec3i = extents, dstOff: Vec3i = Vec3i(0), dstSize: Vec3i = dst.extents) {

		MemoryStack.stackPush().use { stack ->
			val blitRegion = VkImageBlit2.calloc(1, stack)
				.`sType$Default`()
				.srcOffsets(0) { it.set(srcOff) }
				.srcOffsets(1) { it.set(srcSize) }
				.dstOffsets(0) { it.set(dstOff) }
				.dstOffsets(1) { it.set(dstSize) }
				.srcSubresource(VkStructs.createImageLayers(stack, VK13.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1))
				.dstSubresource(VkStructs.createImageLayers(stack, VK13.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1))

			val blitInfo = VkBlitImageInfo2.calloc(stack)
				.`sType$Default`()
				.srcImage(image)
				.srcImageLayout(layout)
				.dstImage(dst.image)
				.dstImageLayout(dst.layout)
				.filter(VK13.VK_FILTER_LINEAR)
				.pRegions(blitRegion)

			VK13.vkCmdBlitImage2(cmd.buffer, blitInfo)
		}
	}

	fun uploadData(cmd: PoolAndBuffer, x: Int, y: Int, z: Int, w: Int, h: Int, d: Int, format: TextureFormat, data: ByteBuffer) {
		val dataSize = w * h * d * 4
		val uploadBuffer = VmaBuffer.create(device, dataSize.toLong(), VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
		cmd.deletion.push(uploadBuffer)

		uploadBuffer.getBuffer(dataSize).put(0, data, 0, data.limit())

		transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)

		if (format == this.format) {
			MemoryStack.stackPush().use { stack ->
				val regions = VkBufferImageCopy.calloc(1, stack)
					.bufferOffset(0)
					.bufferRowLength(0)
					.bufferImageHeight(0)

					.imageSubresource(VkStructs.createImageLayers(stack, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1))
					.imageOffset(VkOffset3D.calloc(stack).set(x, y, z))
					.imageExtent(VkExtent3D.calloc(stack).set(w, h, d))

				cmd.copyBufferToImage(uploadBuffer, this, regions)
			}
		} else {
			MemoryStack.stackPush().use { stack ->
				val blitImage = VkUtil.createImage3D(device, "Blit Image", VK10.VK_IMAGE_TYPE_2D, format, 19, VK10.VK_IMAGE_ASPECT_COLOR_BIT, Vec3i(w, h, d))
				val regions = VkBufferImageCopy.calloc(1, stack)
					.bufferOffset(0)
					.bufferRowLength(0)
					.bufferImageHeight(0)

					.imageSubresource(VkStructs.createImageLayers(stack, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1))
					.imageOffset(VkOffset3D.calloc(stack).set(0, 0, 0))
					.imageExtent(VkExtent3D.calloc(stack).set(w, h, d))
				cmd.copyBufferToImage(uploadBuffer, blitImage, regions)
				blitImage.copyTo(cmd, this, Vec3i(), Vec3i(w, h, d), Vec3i(x, y, z), Vec3i(w, h, d))
				cmd.deletion.push(blitImage)
			}
		}

		transition(cmd, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
	}

	fun uploadData(cmd: PoolAndBuffer, format: TextureFormat, data: ByteBuffer) {
		uploadData(cmd, 0, 0, 0, width, height, depth, format, data)
	}

	fun fetchData(submitter: VulkanImmediateSubmitter, x: Int, y: Int, z: Int, w: Int, h: Int, d: Int): ByteBuffer {
		val dataSize = w * h * d * 4
		MemoryStack.stackPush().use { stack ->
			val fetchBuffer = VmaBuffer.create(device, stack, dataSize.toLong(), VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT, Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
			submitter.submitImmediate { cmd ->
				transition(cmd, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)

				val regions = VkBufferImageCopy.calloc(1, stack)
					.bufferOffset(0)
					.bufferRowLength(0)
					.bufferImageHeight(0)

					.imageSubresource(VkStructs.createImageLayers(stack, VK10.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1))
					.imageOffset(VkOffset3D.calloc(stack).set(x, y, z))
					.imageExtent(VkExtent3D.calloc(stack).set(w, h, d))
				cmd.copyImageToBuffer(fetchBuffer, this, regions)

				transition(cmd, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
			}
			val buffer = stack.malloc(dataSize).put(fetchBuffer.getBuffer(dataSize)).flip()
			fetchBuffer.delete()
			return buffer
		}
	}

	fun fetchData(submitter: VulkanImmediateSubmitter): ByteBuffer {
		return fetchData(submitter, 0, 0, 0, width, height, depth)
	}

	fun getData(pixelSize: Int): ByteBuffer {
		val bytes = width * height * depth * pixelSize

		val allocateInfo = getMemoryAllocateInfo(VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
		val memPointer = MemoryUtil.memAllocLong(1)
		VkUtil.processError(VK13.vkAllocateMemory(device.device, allocateInfo, null, memPointer), "Failed to allocate image memory")
		val memory = memPointer[0]
		memPointer.free()
		VkUtil.processError(VK13.vkBindImageMemory(device.device, image, memory, 0), "Failed to bind image memory")

		val dataPointer = MemoryUtil.memAllocPointer(1)
		VkUtil.processError(VK13.vkMapMemory(device.device, image, 0, bytes.toLong(), 0, dataPointer), "Failed to map image data")
		val data = MemoryUtil.memAlloc(bytes)
			.put(dataPointer.getByteBuffer(bytes))
			.flip()
		VK13.vkUnmapMemory(device.device, memory)

		return data
	}

	fun getSampler() = device.getOrCreateSampler(parameters)
}