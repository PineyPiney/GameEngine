package com.pineypiney.game_engine.visual.menu_items.scroll_lists

import com.pineypiney.game_engine.IGameLogic
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

open class SelectableScrollingListEntry<E: SelectableScrollingListItem>(parent: E, number: Int): InteractableScrollingListEntry<E>(parent, number) {

    override fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {

        val p = super.onPrimary(game, action, mods, cursorPos)

        if(action == GLFW.GLFW_RELEASE && this.hover){
            if(this.index != parent.selectedEntry){
                // Remember to unselect before selecting,
                // as select() changes the selectedEntry value automatically
                parent.getSelectedEntry()?.unselect()
                select()
            }

        }

        return p
    }

    open fun select(){
        this.forceUpdate = true
        parent.selectedEntry = this.index
        parent.action(parent.selectedEntry, this)
    }

    open fun unselect(){
        this.forceUpdate = false
        parent.selectedEntry = -1
        parent.action(-1, null)
    }
}