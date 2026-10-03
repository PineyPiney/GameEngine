package com.pineypiney.game_engine.resources.shaders.vulkan

import com.pineypiney.game_engine.resources.shaders.ShaderStorageBuffer
import com.pineypiney.game_engine.vulkan.VmaBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice
import java.nio.ByteBuffer

class VulkanShaderStorageBuffer(device: VulkanDevice, val name: String, override var size: Int, val usage: Int, val allocUsage: Int) : ShaderStorageBuffer {

	var buffer = VmaBuffer.create(device, size.toLong(), usage, allocUsage, name)

	override fun getData(offset: Long, size: Int): ByteBuffer {
		return buffer.getBuffer(offset, size)
	}

	override fun setData(data: ByteBuffer) {
		if (data.remaining() != size) resize(data.remaining())
		buffer.setBuffer(data, 0L)
	}

	override fun setSubData(data: ByteBuffer, offset: Long) {
		buffer.setBuffer(data, offset)
	}

	override fun resize(size: Int) {
		buffer.delete()
		this.size = size
		buffer = VmaBuffer.create(buffer.device, size.toLong(), usage, allocUsage, name)
	}

	override fun delete() {
		buffer.delete()
	}
}