package com.pineypiney.game_engine.rendering.meshes.vulkan

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanManager

abstract class VulkanMesh(val vulkan: VulkanManager, val attributes: Map<VertexAttribute<*, *>, Long>) : Mesh {

	abstract override val buffers: List<VulkanVertexBuffer>
	abstract val count: Int

	init {

	}

	// These buffers are only accessible on the GPU, so the data will have to be written to them there
	val vertexBuffer: VmaBuffer get() = buffers[0].vertexBuffer
	val vertexBufferAddress: Long get() = buffers[0].vertexBufferAddress

	override fun delete() {
		buffers.delete()
	}
}