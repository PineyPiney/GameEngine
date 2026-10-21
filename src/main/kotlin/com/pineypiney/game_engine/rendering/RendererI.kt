package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.window.Viewport
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

interface RendererI : Initialisable {

	val viewPos: Vec3
	val view: Mat4
	val projection: Mat4
	val guiProjection: Mat4
	val viewportSize: Vec2i
	val aspectRatio: Float

	fun getRenderingApi(): RenderingApi

	fun getViewport(): Viewport = Viewport(Vec2i(0), viewportSize)
}