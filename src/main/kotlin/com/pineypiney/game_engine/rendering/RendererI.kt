package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.window.Viewport
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11C

interface RendererI : Initialisable {

	val viewPos: Vec3
	val view: Mat4
	val projection: Mat4
	val guiProjection: Mat4
	val viewportSize: Vec2i
	val aspectRatio: Float

	fun clear() {
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT or GL11C.GL_DEPTH_BUFFER_BIT or GL11C.GL_STENCIL_BUFFER_BIT)
	}

	fun getViewport(): Viewport
}