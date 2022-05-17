package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ScrollBarItem(val parent: ScrollingListItem, override var origin: Vec2 = Vec2(), override var size: Vec2 = Vec2()): InteractableMenuItem() {

    override var pressed: Boolean = false

    val colour = Vec3(0x00, 0xBF, 0xFF) / 255

    override fun setUniforms() {
        shader.setVec4("colour", Vec4(colour))
    }

    override fun update(interval: Float, time: Double) {

    }

    override fun onDrag(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2) {
        super.onDrag(game, cursorPos, cursorDelta)
        forceUpdate = true

        // If the scroller item is taller, then the same scroll value should move the bar by a smaller amount
        // (Remember that parent.scroll is proportional, a value between 0 and (1-ratio))
        parent.scroll += (cursorDelta.y / (2 * parent.size.y))
    }

    override fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val p = super.onPrimary(game, action, mods, cursorPos)

        if(!pressed) forceUpdate = false

        return p
    }
}