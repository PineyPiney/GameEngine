package com.pineypiney.game_engine.window

import glm_.vec2.Vec2
import glm_.vec2.Vec2i

interface Monitor {

	/**
	 * - [videoMode]: The current Video Mode being used by the monitor
	 * - [videoModes]: All Video Modes supported by the monitor
	 */
	val pos: Vec2i
	val contentScale: Vec2
	val name: String?
	val videoModes: Iterable<VideoMode>
	val videoMode: VideoMode
}