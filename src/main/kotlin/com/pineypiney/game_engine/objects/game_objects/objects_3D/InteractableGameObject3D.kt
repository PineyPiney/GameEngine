package com.pineypiney.game_engine.objects.game_objects.objects_3D

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2

abstract class InteractableGameObject3D(shader: Shader): RenderedGameObject3D(shader), Interactable {

    override val children: MutableSet<Interactable> = mutableSetOf()
    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var importance: Int = 0
    override var pressed: Boolean = false

    override fun checkHover(screenPos: Vec2, worldPos: Vec2): Boolean {
        TODO("I don't want to ;-;")
    }

    override fun update(interval: Float, time: Double) {

    }
}