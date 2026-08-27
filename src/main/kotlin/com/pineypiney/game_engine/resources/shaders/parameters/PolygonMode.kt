package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.util.ApiEnum
import org.lwjgl.opengl.GL11C
import org.lwjgl.vulkan.VK10

enum class PolygonMode(override val opengl: Int, override val vulkan: Int) : ApiEnum {
	POINTS(GL11C.GL_POINT, VK10.VK_POLYGON_MODE_POINT),
	LINE(GL11C.GL_LINE, VK10.VK_POLYGON_MODE_LINE),
	FILL(GL11C.GL_FILL, VK10.VK_POLYGON_MODE_FILL)
}