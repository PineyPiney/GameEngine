package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.extension_functions.isWithin
import glm_.vec2.Vec2

abstract class InteractableGameObject(shader: Shader): RenderedGameObject(shader), Interactable {

    override val children: MutableSet<Interactable> = mutableSetOf()
    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var importance: Int = 0
    override var pressed: Boolean = false
    override val objects: MutableList<ObjectCollection> = mutableListOf()

    override fun checkHover(screenPos: Vec2, worldPos: Vec2): Boolean {
        return worldPos.isWithin(position, scale)
    }

    override fun update(interval: Float, time: Double) {

    }
}