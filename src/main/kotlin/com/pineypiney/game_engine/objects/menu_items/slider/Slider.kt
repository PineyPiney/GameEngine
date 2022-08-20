package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2

abstract class Slider: InteractableMenuItem() {

    abstract val pointer: SliderPointer

    protected abstract val low: Float
    protected abstract val high: Float
    abstract var value: Float
    abstract val window: Window

    private val range: Float; get() = high - low
    val scale: Float; get() = (high - low) / size.x

    @Throws(IllegalArgumentException::class)
    override fun init() {
        super.init()

        if(low > high){
            throw(IllegalArgumentException("Set Slider with low value $low and high value $high, high must be greater than low"))
        }

        pointer.origin = this.origin + Vec2(value/scale, 0)
    }

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setFloatUniform("aspect"){ window.aspectRatio }
    }

    override fun setChildren() {
        addChild(pointer)
    }

    override fun draw() {
        super.draw()
        pointer.drawCenteredBottom(origin + Vec2(value / scale, size.y * 0.2))
    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return super.checkHover(ray, screenPos) || pointer.checkHover(ray, screenPos)
    }

    open fun moveSliderTo(move: Float){
        val relative = (move - origin.x) / size.x
        value = (low + (relative * range)).coerceIn(low, high)
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
    }
}