package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.Visual

abstract class InteractableMenuItem : MenuItem(), Interactable {

    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var pressed: Boolean = false

    override val children: MutableList<Interactable> = mutableListOf()

    override var importance: Int = 0

    override fun init() {
        this.hover = this.checkHover()
    }

    override fun update(interval: Float, time: Double) {
        if(!hover) pressed = false
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)

        children.filterIsInstance<Visual>().forEach { it.updateAspectRatio(window) }
    }

    override fun checkHover(): Boolean {
        return mouseBetween(this.origin, this.size)
    }
}