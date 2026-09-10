package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.opengl.OpenGlObjectRenderer
import com.pineypiney.game_engine.rendering.vulkan.VulkanObjectRenderer
import com.pineypiney.game_engine.rendering.vulkan.VulkanRendering
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

interface ObjectRenderer : RendererI {

	fun setSize(size: Vec2i)

	fun render(obj: GameObject)

	fun getTexture(id: String): Texture2D


	companion object {
		fun create(parent: RendererI, viewPos: Vec3, size: Vec2i, projection: Mat4 = I): ObjectRenderer {
			val api = parent.getRenderingApi()
			return if (api is VulkanRendering) VulkanObjectRenderer(viewPos, api.device, size, projection.translate(0f, 0f, 1f))
			else OpenGlObjectRenderer(viewPos, size, projection)
		}
	}
}