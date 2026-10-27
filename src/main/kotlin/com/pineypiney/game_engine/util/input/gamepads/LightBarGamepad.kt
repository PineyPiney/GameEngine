package com.pineypiney.game_engine.util.input.gamepads

import glm_.vec3.Vec3
import glm_.vec3.Vec3b

interface LightBarGamepad {
	fun setLightBar(colour: Vec3b)
	fun setLightBar(colour: Vec3) = setLightBar(Vec3b(
		(colour.x * 255f).toInt().toUByte().toByte(),
		(colour.y * 255f).toInt().toUByte().toByte(),
		(colour.z * 255f).toInt().toUByte().toByte()
	))
}