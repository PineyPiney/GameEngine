package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.util.input.Inputs
import glm_.vec2.Vec2

abstract class SelectableScrollingListItem(input: Inputs, origin: Vec2, size: Vec2, entryHeight: Float, scrollerWidth: Float, val action: (Int, SelectableScrollingListEntry<*>?) -> Unit = { _: Int, _: SelectableScrollingListEntry<*>? ->}) :
    ScrollingListItem(input, origin, size, entryHeight, scrollerWidth) {

    open var selectedEntry: Int = -1

    open fun getSelectedEntry(): SelectableScrollingListEntry<*>? = getEntry(selectedEntry)
}