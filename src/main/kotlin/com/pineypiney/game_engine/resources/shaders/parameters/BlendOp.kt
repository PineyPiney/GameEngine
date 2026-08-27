package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.util.ApiEnum
import org.lwjgl.opengl.GL14C
import org.lwjgl.vulkan.VK10

enum class BlendOp(override val opengl: Int, override val vulkan: Int) : ApiEnum {
	ADD(GL14C.GL_FUNC_ADD, VK10.VK_BLEND_OP_ADD),
	SUBTRACT(GL14C.GL_FUNC_SUBTRACT, VK10.VK_BLEND_OP_SUBTRACT),
	REVERSE_SUBTRACT(GL14C.GL_FUNC_REVERSE_SUBTRACT, VK10.VK_BLEND_OP_REVERSE_SUBTRACT),
	MIN(GL14C.GL_MIN, VK10.VK_BLEND_OP_MIN),
	MAX(GL14C.GL_MAX, VK10.VK_BLEND_OP_MAX),
}