package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.rendering.ObjectRenderer
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11C

class OpenGlObjectRenderer(override val viewPos: Vec3, viewportSize: Vec2i = Vec2i(64), override val projection: Mat4 = I) : ObjectRenderer {

	private val framebuffer = OpenGlCDSFramebuffer(viewportSize, TextureFormat.RGBA8)

	override val viewportSize: Vec2i get() = framebuffer.size
	override val view: Mat4 = I.translate(viewPos)
	override val guiProjection: Mat4 = projection
	override val aspectRatio: Float = viewportSize.x.toFloat() / viewportSize.y

	override fun init() {
		framebuffer.init()
	}

	override fun setSize(size: Vec2i) {
		framebuffer.setSize(size)
	}

	override fun render(obj: GameObject) {
		framebuffer.bind()
		GLFunc.viewportO = viewportSize
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT or GL11C.GL_DEPTH_BUFFER_BIT or GL11C.GL_STENCIL_BUFFER_BIT)

		val des = obj.allActiveDescendants().flatMap { obj -> obj.components.filterIsInstance<RenderedComponentI>().filter { it.visible } }.sortedBy { it.parent.transformComponent.worldPosition.z }
		for (i in des) {
			i.render(this, 0.0)
		}
	}

	override fun getTexture(id: String): Texture2D {
		return framebuffer.copyTexture(id)
	}

	override fun getRenderingApi(): RenderingApi = OpenGlRendering

	override fun delete() {
		framebuffer.delete()
	}
}