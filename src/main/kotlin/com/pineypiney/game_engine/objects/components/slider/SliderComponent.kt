package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.menu_items.slider.BasicSliderPointer
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec3.Vec3
import kotlin.math.min

abstract class SliderComponent<T: Number>(
	parent: GameObject,
	value: T
) : DefaultInteractorComponent(parent) {

	protected abstract val low: T
	protected abstract val high: T

	open var value: T = value
		set(value) {
			field = value
			pointer.position = Vec3(getDelta(), 0.7f, .01f)
		}

	val pointer: GameObject get() = parent.children.first { it.name == "SliderPointer" }

	abstract val range: Number

	init {
		parent.addChild(BasicSliderPointer(1f))
	}

	@Throws(IllegalArgumentException::class)
	override fun init() {
		super.init()

		if (low.toFloat() > high.toFloat()) {
			throw (IllegalArgumentException("Set Slider with low value $low and high value $high, high must be greater than low"))
		}

		pointer.position = Vec3(getDelta(), 0.7f, .01f)
	}

	abstract fun getDelta(): Float
	abstract fun valueFromDelta(delta: Float): T

	override fun checkHover(ray: Ray, cursor: CursorPosition): Float {
		val slider = super.checkHover(ray, cursor)
		val pointer = pointer.getComponent<DefaultInteractorComponent>()!!
			.checkHover(ray, cursor)
		return if(slider == -1f) pointer
		else min(slider, pointer)
	}

	open fun moveSliderTo(move: Float) {
		val relative = (move - parent.transformComponent.worldPosition.x) / parent.transformComponent.worldScale.x
		value = valueFromDelta(relative)
	}
}