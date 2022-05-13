package com.pineypiney.game_engine.visual.menu_items

import com.pineypiney.game_engine.Window

abstract class StaticInteractableMenuItem : InteractableMenuItem(){

    override fun checkHover(): Boolean{
        val mouse = Window.INSTANCE.input.mouse
        return between(mouse.lastPos, this.origin, this.size)
    }

    override fun update(interval: Float, time: Double) {
        super.update(interval, time)
        hover = checkHover()
    }
}