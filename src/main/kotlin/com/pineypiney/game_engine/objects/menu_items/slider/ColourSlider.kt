package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.widgets.slider.ActionFloatSliderComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.ActionSliderComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.ColourSliderRendererComponent
import com.pineypiney.game_engine.resources.shaders.RenderShader
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class ColourSlider(
	name: String,
	val shader: RenderShader,
	val colours: MutableMap<String, Float>,
	val action: ActionSliderComponent<Float>.() -> Unit = {}
) : GameObject(name, 1) {

	constructor(name: String, origin: Vec2, size: Vec2, shader: RenderShader, colours: MutableMap<String, Float>, action: ActionSliderComponent<Float>.() -> Unit = {}): this(name, shader, colours, action){
		os(origin, size)
	}

	constructor(name: String, pos: Vec2i, size: Vec2i, origin: Vec2, shader: RenderShader, colours: MutableMap<String, Float>, action: ActionSliderComponent<Float>.() -> Unit = {}): this(name, shader, colours, action){
		pixel(pos, size, origin)
	}

	override fun addComponents() {
		super.addComponents()
		components.add(ActionFloatSliderComponent(this, 0f, 255f, 255f, action))
		components.add(ColourSliderRendererComponent(this, shader, colours))
	}
}