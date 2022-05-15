package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.Visual
import glm_.vec2.Vec2

abstract class InteractableMenuItem : MenuItem(), Interactable {

    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var pressed: Boolean = false

    override val children: MutableList<Interactable> = mutableListOf()

    override var importance: Int = 0

    override fun init() {

    }

    override fun update(interval: Float, time: Double) {
        if(!hover) pressed = false
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)

        children.filterIsInstance<Visual>().forEach { it.updateAspectRatio(window) }
    }

    override fun checkHover(screenPos: Vec2, worldPos: Vec2): Boolean {
        return between(screenPos, this.origin, this.size)
    }
}