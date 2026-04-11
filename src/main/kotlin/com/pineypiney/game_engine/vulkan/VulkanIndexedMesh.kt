package com.pineypiney.game_engine.vulkan

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK12
import java.nio.ByteBuffer

open class VulkanIndexedMesh(val vulkan: VulkanManager, verticesData: ByteBuffer, indicesData: ByteBuffer, override val attributes: Map<VertexAttribute<*, *>, Long>) : Mesh {

	override val stride by lazy { this.attributes.keys.sumOf { it.bytes } }
	val count = indicesData.capacity() / 4

	// These buffers are only accessible on the GPU, so the data will have to be written to them there
	val vertexBuffer: VmaBuffer = VmaBuffer.create(
		vulkan.device,
		verticesData.capacity().toLong(),
		VK12.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT or VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT or VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
		Vma.VMA_MEMORY_USAGE_GPU_ONLY
	)
	val indexBuffer: VmaBuffer =
		VmaBuffer.create(vulkan.device, indicesData.capacity().toLong(), VK12.VK_BUFFER_USAGE_INDEX_BUFFER_BIT or VK12.VK_BUFFER_USAGE_TRANSFER_DST_BIT, Vma.VMA_MEMORY_USAGE_GPU_ONLY)
	var vertexBufferAddress: Long = vulkan.device.getBufferAddress(vertexBuffer)

	init {

		// This staging buffer will be filled on the CPU and used to fill the GPU only buffers
		val staging = VmaBuffer.create(vulkan.device, (verticesData.capacity() + indicesData.capacity()).toLong(), VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, Vma.VMA_MEMORY_USAGE_CPU_ONLY)

		// Fill the staging buffer with the data on the CPU
		val b = MemoryUtil.memByteBuffer(staging.info.pMappedData(), verticesData.capacity() + indicesData.capacity())
		b.put(verticesData).put(indicesData)

		// Copy the data from staging into the vertex and index buffers on the GPU
		vulkan.submitter.submitImmediate { cmd ->
			cmd.copyBuffer(staging.buffer, vertexBuffer.buffer, 0L, 0L, verticesData.capacity().toLong())
			cmd.copyBuffer(staging.buffer, indexBuffer.buffer, verticesData.capacity().toLong(), 0L, indicesData.capacity().toLong())
		}

		staging.delete()
//		verticesData.free()
//		indicesData.free()
	}

	override fun bind(api: RenderingApi) {
		api.bindIndices(indexBuffer.buffer, 0L, VK10.VK_INDEX_TYPE_UINT32)
	}

	override fun draw(api: RenderingApi, mode: Int) {
		api.drawIndexed(count, 0, 0)
	}

	override fun drawInstanced(api: RenderingApi, amount: Int, mode: Int) {
		api.drawIndexedInstanced(count, 0, amount, 0, 0)
	}

	override fun getData(): ByteBuffer {
		val size = vertexBuffer.info.size()

		// This staging buffer will be filled on the GPU and then read from the CPU
		val staging = VmaBuffer.create(vulkan.device, size, VK12.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, Vma.VMA_MEMORY_USAGE_CPU_ONLY)

		// Copy the data from vertex buffer into the staging buffer on the GPU
		vulkan.submitter.submitImmediate { cmd ->
			cmd.copyBuffer(vertexBuffer.buffer, staging.buffer, 0L, 0L, size)
		}

		// Read the staging buffer data on the CPU
		val pData = MemoryUtil.memAllocPointer(1).put(staging.info.pMappedData())
		return pData.getByteBuffer(0, size.toInt())
	}

	override fun delete() {
		vertexBuffer.delete()
		indexBuffer.delete()
	}
}