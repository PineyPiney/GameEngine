package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.WindowI
import glm_.vec2.Vec2

class BasicScrollList(override var origin: Vec2, override var size: Vec2, override val entryHeight: Float, override val scrollerWidth: Float, entries: Array<String>, window: WindowI): ScrollingListItem() {

    override val items: List<BasicListEntry> = entries.mapIndexed { i, e -> BasicListEntry(e, this, i, window) }

}