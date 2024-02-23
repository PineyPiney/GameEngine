package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2

abstract class InteractableGameObject2D(shader: Shader): RenderedGameObject2D(shader), Interactable {

    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var importance: Int = 0
    override var pressed: Boolean = false


    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return Rect2D(position2D, scale2D).intersectedBy(ray).isNotEmpty()
    }

    override fun update(interval: Float, time: Double) {

    }
}