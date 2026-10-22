package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.rendering.Framebuffer
import glm_.vec2.Vec2i

interface OpenXrFramebuffer : Framebuffer {

	var colourIndex: Int
	var depthIndex: Int

	fun setTextures(colour: Int, depth: Int)

	override fun setSize(size: Vec2i) {
		throw RuntimeException("Cannot set size for OpenXrFramebuffer")
	}

	override fun setSize(width: Int, height: Int) {
		throw RuntimeException("Cannot set size for OpenXrFramebuffer")
	}
}