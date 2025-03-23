package com.pineypiney.game_engine.objects.components.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.WindowI
import glm_.vec3.Vec3

class SliderPointerComponent(parent: GameObject) : DefaultInteractorComponent(parent), UpdatingAspectRatioComponent {

	val slider: SliderComponent<*>? get() = parent.parent?.getComponent<SliderComponent<*>>()

	override fun init() {
		super.init()
		asp()
	}

	override fun onDrag(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onDrag(window, cursorPos, cursorDelta, ray)
		slider?.moveSliderTo(cursorPos.position.x)
	}

	override fun updateAspectRatio(view: Viewport) {
		asp()
	}

	fun asp(){
		val parentHeight = parent.parent?.transformComponent?.worldScale?.y ?: 1f
		parent.transformComponent.worldScale = Vec3(parentHeight, parentHeight, 1f)
	}
}