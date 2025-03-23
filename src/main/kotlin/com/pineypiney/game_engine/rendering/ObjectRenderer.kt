package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.Viewport
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11C

class ObjectRenderer(override val viewPos: Vec3, override val viewportSize: Vec2i = Vec2i(64)): RendererI {
	override val view: Mat4 = I.translate(viewPos)
	override val projection: Mat4 = I
	override val guiProjection: Mat4 = I
	override val aspectRatio: Float = viewportSize.x.toFloat() / viewportSize.y

	val frameBuffer = FrameBuffer(viewportSize.x, viewportSize.y, GL11C.GL_RGBA)

	override fun init() {
		frameBuffer.generate()
	}

	fun render(obj: GameObject){
		frameBuffer.bind()
		GLFunc.viewportO = viewportSize
		clear()

		val des = obj.allActiveDescendants().mapNotNull { it.getComponent<RenderedComponentI>() }.sortedBy { it.parent.transformComponent.worldPosition.z }
		for(i in des){
			i.render(this, 0.0)
		}
	}

	override fun getViewport(): Viewport {
		return Viewport(Vec2i(0), viewportSize)
	}

	override fun delete() {
		frameBuffer.delete()
	}
}