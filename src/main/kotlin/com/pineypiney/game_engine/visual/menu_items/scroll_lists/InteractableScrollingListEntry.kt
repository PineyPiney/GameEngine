package com.pineypiney.game_engine.visual.menu_items.scroll_lists

import com.pineypiney.game_engine.visual.Interactable
import com.pineypiney.game_engine.visual.ScreenObjectCollection

open class InteractableScrollingListEntry<E: ScrollingListItem>(parent: E, number: Int): ScrollingListEntry<E>(parent, number), Interactable {

    override var forceUpdate: Boolean = false
    override var hover: Boolean = false
    override var pressed: Boolean = false

    override val children: MutableList<Interactable> = mutableListOf()

    override var importance: Int = 0

    override fun update(interval: Float, time: Double) {

    }

    override fun checkHover(): Boolean {
        return mouseBetween(this.origin, this.size)
    }

    override fun addTo(objects: ScreenObjectCollection) {}
    override fun removeFrom(objects: ScreenObjectCollection) {}
}