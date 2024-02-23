package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

abstract class SelectableScrollListEntryComponent(parent: GameObject): ScrollListEntryComponent(parent){

    override val list: SelectableScrollListComponent get() = parent.parent?.getComponent<SelectableScrollListComponent>()!!

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {

        val p = super.onPrimary(window, action, mods, cursorPos)

        if(action == GLFW.GLFW_RELEASE && this.hover){
            if(this.index != list.selectedEntry){
                // Remember to unselect before selecting,
                // as select() changes the selectedEntry value automatically
                list.getSelectedEntry()?.unselect()
                select()
            }
        }

        return p
    }

    open fun select(){
        this.forceUpdate = true
        list.selectedEntry = this.index
        list.action(list.selectedEntry, this)
    }

    open fun unselect(){
        this.forceUpdate = false
        list.selectedEntry = -1
        list.action(-1, null)
    }
}