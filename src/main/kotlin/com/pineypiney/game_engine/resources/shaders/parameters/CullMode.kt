package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.util.ApiEnum
import org.lwjgl.opengl.GL11C
import org.lwjgl.vulkan.VK10

enum class CullMode(override val opengl: Int, override val vulkan: Int) : ApiEnum {
	NONE(GL11C.GL_NONE, VK10.VK_CULL_MODE_NONE),
	FRONT(GL11C.GL_FRONT, VK10.VK_CULL_MODE_FRONT_BIT),
	BACK(GL11C.GL_BACK, VK10.VK_CULL_MODE_BACK_BIT),
	FRONT_AND_BACK(GL11C.GL_FRONT_AND_BACK, VK10.VK_CULL_MODE_FRONT_AND_BACK)
}