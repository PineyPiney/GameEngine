package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2

open class InteractableScrollingListEntry<E: ScrollingListItem>(parent: E, number: Int): ScrollingListEntry<E>(parent, number),
    Interactable {

    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var pressed: Boolean = false

    override val children: MutableSet<Interactable> = mutableSetOf()

    override var importance: Int = 0

    override fun update(interval: Float, time: Double) {

    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return screenPos.isWithin(this.origin, this.size)
    }
}