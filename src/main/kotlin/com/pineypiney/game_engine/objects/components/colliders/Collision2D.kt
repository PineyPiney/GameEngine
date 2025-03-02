package com.pineypiney.game_engine.objects.components.colliders

import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.vec2.Vec2

class Collision2D(val shape1: Shape2D, val shape1Movement: Vec2, val shape2: Shape2D, val shape2Movement: Vec2, val collisionPoint: Vec2, val collisionNormal: Vec2, val removeShape1FromShape2: Vec2) {
}