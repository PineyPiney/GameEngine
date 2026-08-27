package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.util.ApiEnum
import org.lwjgl.opengl.GL11C
import org.lwjgl.vulkan.VK10

enum class CompareOp(override val opengl: Int, override val vulkan: Int) : ApiEnum {
	NEVER(GL11C.GL_NEVER, VK10.VK_COMPARE_OP_NEVER),
	LESS(GL11C.GL_LESS, VK10.VK_COMPARE_OP_LESS),
	EQUAL(GL11C.GL_EQUAL, VK10.VK_COMPARE_OP_EQUAL),
	LEQUAL(GL11C.GL_LEQUAL, VK10.VK_COMPARE_OP_LESS_OR_EQUAL),
	GREATER(GL11C.GL_GREATER, VK10.VK_COMPARE_OP_GREATER),
	NOTEQUAL(GL11C.GL_NOTEQUAL, VK10.VK_COMPARE_OP_NOT_EQUAL),
	GEQUAL(GL11C.GL_GEQUAL, VK10.VK_COMPARE_OP_GREATER_OR_EQUAL),
	ALWAYS(GL11C.GL_ALWAYS, VK10.VK_COMPARE_OP_ALWAYS),
}