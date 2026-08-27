package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.util.ApiEnum
import org.lwjgl.opengl.GL11C
import org.lwjgl.vulkan.VK10

enum class TextureAspect(override val opengl: Int, override val vulkan: Int) : ApiEnum {

	COLOUR(GL11C.GL_COLOR_BUFFER_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT),
	DEPTH(GL11C.GL_DEPTH_BUFFER_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT),
	STENCIL(GL11C.GL_STENCIL_BUFFER_BIT, VK10.VK_IMAGE_ASPECT_STENCIL_BIT)
}