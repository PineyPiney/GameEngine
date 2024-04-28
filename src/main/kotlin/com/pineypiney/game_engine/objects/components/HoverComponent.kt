package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2

class HoverComponent(parent: GameObject, val onEnter: (HoverComponent) -> Unit, val onExit: (HoverComponent) -> Unit): DefaultInteractorComponent(parent, "HVR") {
    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        val h = super.checkHover(ray, screenPos)

        if(h && !hover) onEnter(this)
        else if(!h && hover) onExit(this)

        return h
    }
}