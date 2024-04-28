package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class SliderPointerComponent(parent: GameObject): DefaultInteractorComponent(parent, "SLP") {

    val slider: SliderComponent get() = parent.parent?.getComponent<SliderComponent>()!!

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        val size = Vec2(parent.transformComponent.worldScale) * renderSize
        return screenPos.isWithin(Vec2(parent.transformComponent.worldPosition) - (size * 0.5f), size)
    }

    override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
        super.onDrag(window, cursorPos, cursorDelta, ray)
        slider.moveSliderTo(cursorPos.x)
    }
}