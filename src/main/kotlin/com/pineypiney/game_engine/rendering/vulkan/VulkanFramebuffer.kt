package com.pineypiney.game_engine.rendering.vulkan

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.resources.textures.vulkan.VulkanImage2D
import com.pineypiney.game_engine.vulkan.PoolAndBuffer
import com.pineypiney.game_engine.vulkan.VulkanDevice

interface VulkanFramebuffer : Framebuffer {

	val device: VulkanDevice
	override val colour: VulkanImage2D
	override val depthStencil: VulkanImage2D

	fun begin(cmd: PoolAndBuffer)
	fun end(cmd: PoolAndBuffer)
}