package com.pineypiney.game_engine.objects.menu_items.scroll_lists

abstract class SelectableScrollingListItem : ScrollingListItem() {

    abstract val action: (Int, SelectableScrollingListEntry<*>?) -> Unit
    abstract override val items: List<SelectableScrollingListEntry<*>>

    open var selectedEntry: Int = -1

    open fun getSelectedEntry(): SelectableScrollingListEntry<*>? = items.getOrNull(selectedEntry)
}