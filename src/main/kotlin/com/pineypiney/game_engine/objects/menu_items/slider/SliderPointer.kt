package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.MovableDrawable
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import glm_.vec2.Vec2

abstract class SliderPointer: InteractableMenuItem(), MovableDrawable {

    abstract val parent: Slider
    abstract val height: Float

    // Make origin variable so that kotlin generates a setter function for it
    override var origin: Vec2 = super.origin

    override fun onDrag(game: GameLogicI, cursorPos: Vec2, cursorDelta: Vec2) {
        super.onDrag(game, cursorPos, cursorDelta)

        parent.moveSliderTo(cursorPos.x)
    }
}