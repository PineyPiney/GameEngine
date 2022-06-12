package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import glm_.vec2.Vec2

abstract class SelectableScrollingListItem(origin: Vec2, size: Vec2, entryHeight: Float, scrollerWidth: Float, val action: (Int, SelectableScrollingListEntry<*>?) -> Unit = { _: Int, _: SelectableScrollingListEntry<*>? ->}) :
    ScrollingListItem(origin, size, entryHeight, scrollerWidth) {

    abstract override val items: List<SelectableScrollingListEntry<*>>

    open var selectedEntry: Int = -1

    open fun getSelectedEntry(): SelectableScrollingListEntry<*>? = items.getOrNull(selectedEntry)
}