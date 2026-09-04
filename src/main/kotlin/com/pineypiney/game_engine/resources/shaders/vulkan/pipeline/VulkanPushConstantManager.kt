package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import kool.free
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VkPushConstantRange
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VulkanPushConstantManager(val pushConstants: Map<String, Pair<DataType, Int>>, val stageSections: Map<Int, Pair<Int, Int>>) : Deletable {

	val pushConstantBuffer = MemoryUtil.memAlloc(stageSections.maxOfOrNull { (_, p) -> p.first + p.second } ?: 0)

	fun getBufferSlice(constantName: String): ByteBuffer? {
		val dotI = constantName.indexOf('.')
		for ((name, pushConstant) in pushConstants) {
			if (name == constantName) return pushConstantBuffer.slice(pushConstant.second, pushConstant.first.size).order(ByteOrder.nativeOrder())
			val (type, offset) = pushConstant
			if (dotI != -1 && type is DataType.Struct && name == constantName.substring(0, dotI)) {
				return type.getBuffer(pushConstantBuffer, offset, constantName.substring(dotI + 1))
			}
		}
		return null
	}

	fun createRanges(stack: MemoryStack): VkPushConstantRange.Buffer {
		val ranges = VkPushConstantRange.calloc(stageSections.size, stack)
		for ((stage, bounds) in stageSections) ranges.get().set(stage, bounds.first, bounds.second)
		return ranges.flip()
	}

	fun push(cmd: PoolAndBuffer, pipeline: VulkanPipeline) {
		for ((stageMask, bounds) in stageSections) {
			cmd.pushConstants(pipeline, stageMask, bounds.first, pushConstantBuffer.slice(bounds.first, bounds.second).order(ByteOrder.nativeOrder()))
		}
	}

	override fun delete() {
		pushConstantBuffer.free()
	}

	companion object {
//		fun create()
	}
}