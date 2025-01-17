package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.components.slider.ActionFloatSliderComponent
import com.pineypiney.game_engine.objects.components.slider.ActionSliderComponent
import com.pineypiney.game_engine.objects.components.slider.ColourSliderRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2

class ColourSlider(
	origin: Vec2,
	size: Vec2,
	val shader: Shader,
	val colours: MutableMap<String, Float>,
	val action: ActionSliderComponent<Float>.() -> Unit = {}
) : MenuItem() {

	init {
		os(origin, size)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(ActionFloatSliderComponent(this, 0f, 255f, 255f, action))
		components.add(ColourSliderRendererComponent(this, shader, colours))
	}
}