package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.rendering.TextureCopier
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.Texture3D
import com.pineypiney.game_engine.resources.textures.TextureAspect
import com.pineypiney.game_engine.resources.textures.parameters.TextureFilter
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.util.vulkanMask
import com.pineypiney.game_engine.vulkan.*
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkBlitImageInfo2
import org.lwjgl.vulkan.VkImageBlit2

class VulkanTextureCopier(device: VulkanDevice) : TextureCopier() {

	lateinit var blitInfo: VkBlitImageInfo2

	val cmd: PoolAndBuffer
	val fence: VulkanFence

	init {
		MemoryStack.stackPush().use { stack ->
			cmd = PoolAndBuffer.create(device, stack, "Texture Copier")
			fence = device.createFence(stack, VK10.VK_FENCE_CREATE_SIGNALED_BIT, "Texture Copier")
		}
	}

	val layouts = mutableMapOf<VulkanImage, Int>()

	override fun init() {
		blitInfo = VkBlitImageInfo2.calloc().`sType$Default`()
	}

	override fun start() {
		fence.reset()
		cmd.resetBuffer()
		cmd.begin(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
	}

	fun setLayout(img: VulkanImage, layout: Int) {
		if (!layouts.containsKey(img)) layouts[img] = img.layout
		img.transition(cmd, layout)
	}

	override fun setSrc(src: Texture2D) {
		if (src is VulkanImage) {
			setLayout(src, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
			srcSize = Vec3i(src.size, 1)
			blitInfo
				.srcImage(src.image)
				.srcImageLayout(src.layout)
		}
	}

	override fun setSrc(src: Texture3D, layer: Int) {
		if (src is VulkanImage) {
			setLayout(src, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
			srcSize = src.size
			blitInfo
				.srcImage(src.image)
				.srcImageLayout(src.layout)
		}
	}

	override fun setDst(dst: Texture2D) {
		if (dst is VulkanImage) {
			setLayout(dst, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
			dstSize = Vec3i(dst.size, 1)
			blitInfo
				.dstImage(dst.image)
				.dstImageLayout(dst.layout)
		}
	}

	override fun setDst(dst: Texture3D, layer: Int) {
		if (dst is VulkanImage) {
			setLayout(dst, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
			dstSize = dst.size
			blitInfo
				.dstImage(dst.image)
				.dstImageLayout(dst.layout)
		}
	}

	override fun copyTexture(srcOrigin: Vec2i, srcTR: Vec2i, dstOrigin: Vec2i, dstTR: Vec2i, mask: Collection<TextureAspect>, filter: TextureFilter) {
		MemoryStack.stackPush().use { stack ->
			val blitRegion = VkImageBlit2.calloc(1, stack)
				.`sType$Default`()
				.srcOffsets(0) { it.set(srcOrigin, 0) }
				.srcOffsets(1) { it.set(srcTR, 1) }
				.dstOffsets(0) { it.set(dstOrigin, 0) }
				.dstOffsets(1) { it.set(dstTR, 1) }
				.srcSubresource(VkStructs.createImageLayers(stack, mask.vulkanMask(), 0, 0, 1))
				.dstSubresource(VkStructs.createImageLayers(stack, mask.vulkanMask(), 0, 0, 1))

			blitInfo
				.filter(filter.vulkanImage)
				.pRegions(blitRegion)

			VK13.vkCmdBlitImage2(cmd.buffer, blitInfo)
		}
	}

	override fun copyTexture(srcOrigin: Vec3i, srcTR: Vec3i, dstOrigin: Vec3i, dstTR: Vec3i, mask: Collection<TextureAspect>, filter: TextureFilter) {
		MemoryStack.stackPush().use { stack ->
			val blitRegion = VkImageBlit2.calloc(1, stack)
				.`sType$Default`()
				.srcOffsets(0) { it.set(srcOrigin) }
				.srcOffsets(1) { it.set(srcTR) }
				.dstOffsets(0) { it.set(dstOrigin) }
				.dstOffsets(1) { it.set(dstTR) }
				.srcSubresource(VkStructs.createImageLayers(stack, mask.vulkanMask(), 0, 0, 1))
				.dstSubresource(VkStructs.createImageLayers(stack, mask.vulkanMask(), 0, 0, 1))

			blitInfo
				.filter(filter.vulkanImage)
				.pRegions(blitRegion)

			VK13.vkCmdBlitImage2(cmd.buffer, blitInfo)
		}
	}

	override fun execute() {
		for ((img, layout) in layouts) img.transition(cmd, layout)
		cmd.end()

		MemoryStack.stackPush().use { stack ->
			val cmdInfo = VkStructs.createBufferSubmits(stack, cmd.buffer, 0)
			val submitInfo = VkStructs.createSubmitInfo2s(stack, cmdInfo, null, null)
			val queue = fence.device.getQueue(0)
			VK13.vkQueueSubmit2(queue, submitInfo, fence.handle)
		}
		fence.wait(1e9)
	}

	override fun delete() {
		blitInfo.free()
		cmd.delete()
		fence.delete()
	}
}