package com.pineypiney.game_engine.util.input.gamepads

import glm_.vec3.Vec3

interface SensorGamepad {
	val acceleration: Vec3
	val angularVelocity: Vec3
}