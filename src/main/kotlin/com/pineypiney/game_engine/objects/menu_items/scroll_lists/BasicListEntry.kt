package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class BasicListEntry(textS: String, parent: BasicScrollList, override val index: Int, window: WindowI): ScrollingListEntry<BasicScrollList>(parent) {

    val text: ScrollerText = ScrollerText(textS, window, limits, size)

    override fun init() {
        super.init()
        text.init()
    }

    override fun draw() {
        super.draw()
        text.drawCenteredLeft(origin + Vec2(0, size.y / 2))
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        text.updateAspectRatio(window)
    }

    override fun delete() {
        super.delete()
        text.delete()
    }
}