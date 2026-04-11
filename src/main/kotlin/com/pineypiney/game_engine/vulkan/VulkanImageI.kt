package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.resources.Resource
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import kool.free
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.*
import java.nio.ByteBuffer

interface VulkanImageI : Resource {

	val device: VulkanDevice
	val image: Long
	val imageView: Long
	val extents: Vec3i
	val format: Int
	var layout: Int


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

	fun transition(cmd: VkCommandBuffer, newLayout: Int, keepData: Boolean = true) {
		val aspectMask = if (newLayout == VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL) VK10.VK_IMAGE_ASPECT_DEPTH_BIT else VK10.VK_IMAGE_ASPECT_COLOR_BIT
		val imageBarrier = VkImageMemoryBarrier2.calloc(1)
			.`sType$Default`()
			.srcStageMask(VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
			.srcAccessMask(VK13.VK_ACCESS_2_MEMORY_WRITE_BIT)
			.dstStageMask(VK13.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT)
			.dstAccessMask(VK13.VK_ACCESS_2_MEMORY_WRITE_BIT or VK13.VK_ACCESS_2_MEMORY_READ_BIT)
			.oldLayout(if (keepData) layout else VK10.VK_IMAGE_LAYOUT_UNDEFINED)
			.newLayout(newLayout)
			.image(image)
			.subresourceRange(VkStructs.createImageRange(aspectMask))

		val dependInfo = VkDependencyInfo.calloc()
			.`sType$Default`()
			.pImageMemoryBarriers(imageBarrier)

		VK13.vkCmdPipelineBarrier2(cmd, dependInfo)
		layout = newLayout
		dependInfo.free()
		imageBarrier.free()
	}

	fun copyTo(cmd: VkCommandBuffer, dst: VulkanImageI, srcSize: Vec2i, dstSize: Vec2i) {

		val blitRegion = VkImageBlit2.calloc(1)
			.`sType$Default`()
			.srcOffsets(1, VkOffset3D.calloc().set(Vec3i(srcSize, 1)))
			.dstOffsets(1, VkOffset3D.calloc().set(Vec3i(dstSize, 1)))
			.srcSubresource(VkStructs.createImageLayers(VK13.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1))
			.dstSubresource(VkStructs.createImageLayers(VK13.VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1))

		val blitInfo = VkBlitImageInfo2.calloc()
			.`sType$Default`()
			.srcImage(image)
			.srcImageLayout(layout)
			.dstImage(dst.image)
			.dstImageLayout(dst.layout)
			.filter(VK13.VK_FILTER_LINEAR)
			.pRegions(blitRegion)

		VK13.vkCmdBlitImage2(cmd, blitInfo)
//		blitInfo.free()
//		blitRegion.free()
	}

	fun getData(pixelSize: Int): ByteBuffer {
		val bytes = extents.x * extents.y * extents.z * pixelSize

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

}