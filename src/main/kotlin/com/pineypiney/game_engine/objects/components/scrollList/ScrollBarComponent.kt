package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class ScrollBarComponent(parent: GameObject): InteractorComponent(parent, "SLB"){

    override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2) {
        super.onDrag(window, cursorPos, cursorDelta)
        forceUpdate = true

        // If the scroller item is taller, then the same scroll value should move the bar by a smaller amount
        // (Remember that parent.scroll is proportional, a value between 0 and (1-ratio))
        parent.parent!!.getComponent<ScrollListComponent>()!!.scroll -= (cursorDelta.y / (parent.parent!!.scale.y))
    }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val p = super.onPrimary(window, action, mods, cursorPos)

        if(!pressed) forceUpdate = false

        return p
    }
}