package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.Window
import glm_.vec2.Vec2

class BasicScrollList(origin: Vec2, size: Vec2, entryHeight: Float, scrollerWidth: Float, entries: Array<String>, window: Window): ScrollingListItem(origin, size, entryHeight, scrollerWidth) {

    override val items: List<BasicListEntry> = entries.mapIndexed { i, e -> BasicListEntry(e, this, i, window) }

}