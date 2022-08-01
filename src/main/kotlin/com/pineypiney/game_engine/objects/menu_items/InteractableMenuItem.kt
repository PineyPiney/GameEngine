package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2

abstract class InteractableMenuItem : MenuItem(), Interactable {

    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var pressed: Boolean = false

    override val children: MutableSet<Interactable> = mutableSetOf()

    override var importance: Int = 0

    override fun init() {
        super.init()

        setChildren()
        for(i in children.filterIsInstance<Initialisable>()) i.init()
    }

    open fun setChildren(){}

    override fun update(interval: Float, time: Double) {
        if(!hover) pressed = false
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)

        children.filterIsInstance<Drawable>().forEach { it.updateAspectRatio(window) }
    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return screenPos.isWithin(this.origin, this.size)
    }
}