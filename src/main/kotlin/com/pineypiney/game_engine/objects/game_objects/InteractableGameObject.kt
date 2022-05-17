package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.util.extension_functions.isWithin
import glm_.vec2.Vec2

abstract class InteractableGameObject: RenderedGameObject(), Interactable {

    override val children: MutableList<Interactable> = mutableListOf()
    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var importance: Int = 0
    override var pressed: Boolean = false
    override val objects: MutableList<ScreenObjectCollection> = mutableListOf()

    override fun checkHover(screenPos: Vec2, worldPos: Vec2): Boolean {
        return worldPos.isWithin(position, scale)
    }

    override fun update(interval: Float, time: Double) {

    }
}