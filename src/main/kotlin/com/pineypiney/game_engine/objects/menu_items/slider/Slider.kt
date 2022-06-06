package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import glm_.vec2.Vec2

abstract class Slider @Throws(IllegalArgumentException::class) constructor(final override val size: Vec2, private val low: Float, private val high: Float, value: Float, val window: Window): InteractableMenuItem() {

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
        super.init()
        pointer.origin = this.origin + Vec2(value/scale, 0)
    }

    override fun setChildren() {
        addChild(pointer)
    }

    override fun draw() {
        super.draw()
        pointer.drawCenteredBottom(origin + Vec2(value / scale, size.y * 0.2))
    }

    override fun checkHover(screenPos: Vec2, worldPos: Vec2): Boolean {
        return super.checkHover(screenPos, worldPos) || pointer.checkHover(screenPos, worldPos)
    }

    open fun moveSliderTo(move: Float){
        val relative = (move - origin.x) / size.x
        value = low + (relative * range)
    }
}