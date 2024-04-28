package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.menu_items.slider.BasicSliderPointer
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class SliderComponent(parent: GameObject, protected open val low: Float, protected open val high: Float, value: Float): DefaultInteractorComponent(parent, "SLD") {

    open var value: Float = value
        set(value) {
            field = value
            pointer.position = Vec3((value - low) / range, 0.7f, 0f)
        }

    val pointer: GameObject get() = parent.children.first { it.name == "SliderPointer" }

    protected val range: Float get() = high - low

    init {
        parent.addChild(BasicSliderPointer(1f))
    }

    @Throws(IllegalArgumentException::class)
    override fun init() {
        super.init()

        if(low > high){
            throw(IllegalArgumentException("Set Slider with low value $low and high value $high, high must be greater than low"))
        }

        pointer.position = Vec3((value - low) / range, 0.7f, 0f)
    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return super.checkHover(ray, screenPos) || pointer.getComponent<DefaultInteractorComponent>()!!.checkHover(ray, screenPos)
    }

    open fun moveSliderTo(move: Float){
        val relative = (move - parent.transformComponent.worldPosition.x) / parent.transformComponent.worldScale.x
        value = (low + (relative * range)).coerceIn(low, high)
    }
}