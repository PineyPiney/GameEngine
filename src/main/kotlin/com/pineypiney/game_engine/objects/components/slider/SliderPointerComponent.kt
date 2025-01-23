package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class SliderPointerComponent(parent: GameObject) : DefaultInteractorComponent(parent), UpdatingAspectRatioComponent {

	val slider: SliderComponent<*>? get() = parent.parent?.getComponent<SliderComponent<*>>()

	override fun init() {
		super.init()
		asp()
	}

	override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onDrag(window, cursorPos, cursorDelta, ray)
		slider?.moveSliderTo(cursorPos.x)
	}

	override fun updateAspectRatio(renderer: RendererI) {
		asp()
	}

	fun asp(){
		val parentHeight = parent.parent?.transformComponent?.worldScale?.y ?: 1f
		parent.transformComponent.worldScale = Vec3(parentHeight, parentHeight, 1f)
	}
}