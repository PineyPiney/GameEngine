package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.Window
import glm_.vec2.Vec2

class BasicListEntry(val name: String, parent: BasicScrollList, index: Int, window: Window): ScrollingListEntry<BasicScrollList>(parent, index) {

    val text = ScrollerText(name, window, size, limits)

    override fun init() {
        super.init()
        text.init()
    }

    override fun draw() {
        super.draw()
        text.drawCenteredLeft(origin + Vec2(0, size.y / 2))
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
        text.updateAspectRatio(window)
    }

    override fun delete() {
        super.delete()
        text.delete()
    }
}