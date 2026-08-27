package com.pineypiney.game_engine.resources.textures.parameters

import com.pineypiney.game_engine.util.ApiEnum
import org.lwjgl.opengl.GL44C
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK12

enum class TextureWrap(override val opengl: Int, override val vulkan: Int) : ApiEnum {
	REPEAT(GL44C.GL_REPEAT, VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT),
	MIRRORED_REPEAT(GL44C.GL_MIRRORED_REPEAT, VK10.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT),
	CLAMP_TO_EDGE(GL44C.GL_CLAMP_TO_EDGE, VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE),
	CLAMP_TO_BORDER(GL44C.GL_CLAMP_TO_BORDER, VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER),
	MIRROR_CLAMP_TO_EDGE(GL44C.GL_MIRROR_CLAMP_TO_EDGE, VK12.VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE),
}