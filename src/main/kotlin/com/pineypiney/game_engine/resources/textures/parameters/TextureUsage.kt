package com.pineypiney.game_engine.resources.textures.parameters

import org.lwjgl.vulkan.VK10

enum class TextureUsage(val vulkan: Int) {

	SRC(VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT),
	DST(VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT),

	// Shader Sampler
	SAMPLER(VK10.VK_IMAGE_USAGE_SAMPLED_BIT),

	// Shader Storage Image
	STORAGE(VK10.VK_IMAGE_USAGE_STORAGE_BIT),

	// Framebuffer Colour Image
	COLOUR(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT),

	// Framebuffer Depth/Stencil Image
	DEPTH_STENCIL(VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT),


}