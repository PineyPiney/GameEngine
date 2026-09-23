package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.resources.textures.Texture2D
import glm_.vec2.Vec2i

interface Framebuffer : Initialisable {

	val colour: Texture2D
	val depthStencil: Texture2D
	val size: Vec2i

	fun setSize(width: Int, height: Int)
	fun setSize(size: Vec2i)
}