package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.util.extension_functions.isBetween
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.glm.max
import glm_.glm.min
import glm_.vec2.Vec2

open class InteractableScrollingListEntry<E: ScrollingListItem>(parent: E, override val index: Int): ScrollingListEntry<E>(parent),
    Interactable {

    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var pressed: Boolean = false

    override var importance: Int = 0

    override fun update(interval: Float, time: Double) {

    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return screenPos.isBetween(max(parent.origin, origin), min(parent.origin + parent.size, origin + size))
    }
}