package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import com.pineypiney.game_engine.util.input.InputState
import glm_.vec2.Vec2

abstract class Slider @Throws(IllegalArgumentException::class) constructor(final override val size: Vec2, private val low: Float, private val high: Float, value: Float): InteractableMenuItem() {

    abstract val pointer: SliderPointer
    private val range: Float = high - low
    val scale: Float = range / size.x

    var value: Float = value
        set(value) {
            field = value.coerceIn(low, high)
        }

    init {
        if(low > high){
            throw(IllegalArgumentException("Set Slider with low value $low and high value $high, high must be greater than low"))
        }
    }

    override fun init() {
        pointer.origin = this.origin + Vec2(value/scale, 0)

        pointer.init()
        addChild(pointer)
    }

    override fun draw() {
        super.draw()
        pointer.drawCenteredBottom(origin + Vec2(value / scale, size.y * 0.2))
    }

    override fun checkHover(screenPos: Vec2, worldPos: Vec2): Boolean {
        return super.checkHover(screenPos, worldPos) || pointer.checkHover(screenPos, worldPos)
    }

    override fun onInput(game: IGameLogic, input: InputState, action: Int, cursorPos: Vec2): Int {
        if(pointer.shouldUpdate()) pointer.onInput(game, input, action, cursorPos)
        return super.onInput(game, input, action, cursorPos)
    }

    open fun moveSliderTo(move: Float){
        val relative = (move - origin.x) / size.x
        value = low + (relative * range)
    }
}