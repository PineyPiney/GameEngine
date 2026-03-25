package com.pineypiney.game_engine.objects.components.widgets.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addAll
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

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)
		return INTERRUPT
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

	companion object {
		val pointerTexture = TextureLoader.getTexture(ResourceKey("menu_items/slider/pointer"))

		fun createBasicPointer(height: Float): SliderPointerComponent {
			val obj = GameObject("Slider Pointer", 1)
			val pointer = SliderPointerComponent(obj)
			obj.components.addAll(
				pointer,
				SpriteComponent(
					obj,
					pointerTexture,
					pointerTexture.height.toFloat() / height,
					SpriteComponent.menuShader
				)
			)
			return pointer
		}
	}
}