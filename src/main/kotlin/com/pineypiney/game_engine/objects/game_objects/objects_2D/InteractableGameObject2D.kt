package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.maths.shapes.Rect
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class InteractableGameObject2D(shader: Shader): RenderedGameObject2D(shader), Interactable {

    override val children: MutableSet<Interactable> = mutableSetOf()
    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var importance: Int = 0
    override var pressed: Boolean = false

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return ray.passesThroughRect(Rect(Vec3(position), Vec3(scale.x, 0, 0), Vec3(0, scale.y, 0)))

    }

    override fun update(interval: Float, time: Double) {

    }
}