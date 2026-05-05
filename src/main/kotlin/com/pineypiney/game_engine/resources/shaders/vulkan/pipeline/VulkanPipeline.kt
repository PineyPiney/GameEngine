package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.shaders.DataType
import com.pineypiney.game_engine.resources.shaders.ShaderStage
import com.pineypiney.game_engine.resources.shaders.vulkan.VulkanShaderData
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage
import com.pineypiney.game_engine.vulkan.*
import org.lwjgl.vulkan.VK10
import java.nio.ByteBuffer

abstract class VulkanPipeline(val pipeline: Long, val layout: VulkanPipelineLayout) : Deletable {

	val device get() = layout.device
	val descriptorLayouts get() = layout.descriptorLayouts

	fun bind(api: RenderingApi) {
		api.bindPipeline(pipeline, getBindPoint())
	}

	abstract fun getBindPoint(): Int

	fun setImage(name: String, image: VulkanImage, imageLayout: Int = image.layout, sampler: Long = image.getSampler()) {
		for (descriptorLayout in descriptorLayouts) {
			for (binding in descriptorLayout.bindings) {
				if (binding.contains(name) && binding is VulkanDescriptorBinding.Image) {
					binding.setImage(image, imageLayout, sampler)
					return
				}
			}
		}
	}

	fun setBuffer(name: String, buffer: ByteBuffer) {
		for ((_, pushConstant) in layout.pushConstantBuffers) {
			if (pushConstant.contains(name)) {
				pushConstant.set(name, buffer)
				return
			}
		}
		for (layout in descriptorLayouts) {
			for (binding in layout.bindings) {
				if (binding.contains(name) && binding is VulkanDescriptorBinding.Buffer) {
					binding.set(name, buffer)
					return
				}
			}
		}
	}

	fun getBuffer(name: String): ByteBuffer? {
		for ((_, pushConstant) in layout.pushConstantBuffers) {
			if (pushConstant.contains(name)) {
				return pushConstant.get(name)
			}
		}
		for (layout in descriptorLayouts) {
			for (binding in layout.bindings) {
				if (binding.contains(name) && binding is VulkanDescriptorBinding.Buffer) {
					return binding.get(name)
				}
			}
		}
		return null
	}

	fun updateDescriptors(commands: PoolAndBuffer, descriptorAllocator: VulkanDescriptorAllocator) {

		val sets = descriptorLayouts.map(descriptorAllocator::allocateDescriptorSet)

		val writer = VulkanDescriptorWriter()
		for (imageSet in sets) {
			for (binding in imageSet.layout.bindings) {
				binding.bind(writer)
			}
			writer.updateSet(device, imageSet).clear()
		}

		commands.bindDescriptorSets(this, sets)
	}

	fun updatePushConstants(frameObjects: VulkanFrameObjects) {
		for ((stage, pushConstants) in layout.pushConstantBuffers) {
			frameObjects.commands.pushConstants(this, stage.vulkan, pushConstants.buffer.getBuffer(pushConstants.size.toInt()))
		}
	}

	override fun delete() {
		VK10.vkDestroyPipeline(device.device, pipeline, null)
		layout.delete()
	}

	companion object {
		fun compileDescriptorLayouts(device: VulkanDevice, data: Map<ShaderStage, VulkanShaderData>): List<VulkanDescriptorLayout> {
			val list = mutableListOf<VulkanDescriptorLayout>()
			for ((stage, stageData) in data) {
				val builders = mutableMapOf<Int, VulkanDescriptorLayout.Builder>()
				for ((name, uniform) in stageData.uniforms) {
					val builder = builders.getOrPut(uniform.set, VulkanDescriptorLayout::Builder)
					when (val data = uniform.data) {
						is DataType.Sampler -> builder.addCombinedImage(uniform.binding, name)
						is DataType.Image -> builder.addStorageImage(uniform.binding, name)
						is DataType.Struct -> builder.addStorageBuffer(device, uniform.binding, data.size, data.getOffsets())
					}
				}
				for ((_, builder) in builders) list.add(builder.build(device, stage.vulkan))
			}
			if (list.isEmpty()) list.add(VulkanDescriptorLayout.Builder().build(device, 0))
			return list
		}
	}

	abstract class Builder<P : VulkanPipeline> : Deletable {

		var layout: VulkanPipelineLayout? = null

		abstract fun build(device: VulkanDevice): P
	}
}