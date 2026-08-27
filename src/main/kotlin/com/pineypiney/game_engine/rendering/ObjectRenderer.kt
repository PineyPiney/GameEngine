package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.opengl.OpenGlObjectRenderer
import com.pineypiney.game_engine.rendering.vulkan.VulkanObjectRenderer
import com.pineypiney.game_engine.rendering.vulkan.VulkanRendering
import com.pineypiney.game_engine.resources.textures.Texture2D
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

interface ObjectRenderer : RendererI {

	fun setSize(size: Vec2i)

	fun render(obj: GameObject)

	fun getTexture(id: String): Texture2D


	companion object {
		fun create(parent: RendererI, viewPos: Vec3, size: Vec2i): ObjectRenderer {
			val api = parent.getRenderingApi()
			return if (api is VulkanRendering) VulkanObjectRenderer(viewPos, api.device, size)
			else OpenGlObjectRenderer(viewPos, size)
		}
	}
}