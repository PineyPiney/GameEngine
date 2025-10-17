package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.SliderPointerComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey

open class BasicSliderPointer(val height: Float) : MenuItem() {

	override var name: String = "SliderPointer"

	override fun addComponents() {
		super.addComponents()
		components.add(SliderPointerComponent(this))
		components.add(
			SpriteComponent(
				this@BasicSliderPointer,
				pointerTexture,
				pointerTexture.height.toFloat() / height,
				SpriteComponent.menuShader
			)
		)
	}

	companion object {
		val pointerTexture = TextureLoader.getTexture(ResourceKey("menu_items/slider/pointer"))
	}
}