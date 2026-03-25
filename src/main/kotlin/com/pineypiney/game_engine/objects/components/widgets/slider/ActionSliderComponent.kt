package com.pineypiney.game_engine.objects.components.widgets.slider

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.util.extension_functions.addAll
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

abstract class ActionSliderComponent<T: Number>(
	parent: GameObject,
	value: T,
	val action: (ActionSliderComponent<T>) -> Unit
) : SliderComponent<T>(parent, value) {

	override var value: T
		get() = super.value
		set(value) {
			super.value = value
			action(this)
		}

	companion object {

		fun createFloatSlider(name: String, low: Float, high: Float, value: Float, action: (ActionSliderComponent<Float>) -> Unit): ActionFloatSliderComponent {
			val obj = GameObject(name, 1)
			return addBasicSlider(obj, low, high, value, ::ActionFloatSliderComponent, action)
		}

		fun createFloatSliderAtRelative(name: String, origin: Vec2, size: Vec2, low: Float, high: Float, value: Float, action: (ActionSliderComponent<Float>) -> Unit): ActionFloatSliderComponent {
			val obj = GameObject(name, 1)
			obj.relative(origin, size)
			return addBasicSlider(obj, low, high, value, ::ActionFloatSliderComponent, action)
		}

		fun createFloatSliderAt(name: String, origin: Vec2, size: Vec2, low: Float, high: Float, value: Float, action: (ActionSliderComponent<Float>) -> Unit): ActionFloatSliderComponent {
			val obj = GameObject(name, 1)
			obj.os(origin, size)
			return addBasicSlider(obj, low, high, value, ::ActionFloatSliderComponent, action)
		}

		fun createFloatSliderAtPixel(
			name: String,
			pos: Vec2i,
			size: Vec2i,
			origin: Vec2,
			low: Float,
			high: Float,
			value: Float,
			action: (ActionSliderComponent<Float>) -> Unit
		): ActionFloatSliderComponent {
			val obj = GameObject(name, 1)
			obj.pixel(pos, size, origin)
			return addBasicSlider(obj, low, high, value, ::ActionFloatSliderComponent, action)
		}

		fun createIntSlider(name: String, low: Int, high: Int, value: Int, action: (ActionSliderComponent<Int>) -> Unit): ActionIntSliderComponent {
			val obj = GameObject(name, 1)
			return addBasicSlider(obj, low, high, value, ::ActionIntSliderComponent, action)
		}

		fun createIntSliderAt(name: String, origin: Vec2, size: Vec2, low: Int, high: Int, value: Int, action: (ActionSliderComponent<Int>) -> Unit): ActionIntSliderComponent {
			val obj = GameObject(name, 1)
			obj.os(origin, size)
			return addBasicSlider(obj, low, high, value, ::ActionIntSliderComponent, action)
		}

		fun createIntSliderAtRelative(name: String, origin: Vec2, size: Vec2, low: Int, high: Int, value: Int, action: (ActionSliderComponent<Int>) -> Unit): ActionIntSliderComponent {
			val obj = GameObject(name, 1)
			obj.relative(origin, size)
			return addBasicSlider(obj, low, high, value, ::ActionIntSliderComponent, action)
		}

		fun createIntSliderAtPixel(name: String, pos: Vec2i, size: Vec2i, origin: Vec2, low: Int, high: Int, value: Int, action: (ActionSliderComponent<Int>) -> Unit): ActionIntSliderComponent {
			val obj = GameObject(name, 1)
			obj.pixel(pos, size, origin)
			return addBasicSlider(obj, low, high, value, ::ActionIntSliderComponent, action)
		}

		inline fun <reified E : Number, S : ActionSliderComponent<E>> addBasicSlider(
			obj: GameObject,
			low: E,
			high: E,
			value: E,
			slider: (GameObject, E, E, E, (ActionSliderComponent<E>) -> Unit) -> S,
			noinline action: (ActionSliderComponent<E>) -> Unit
		): S {
			val slider = slider(obj, low, high, value, action)
			obj.components.addAll(
				slider,
				OutlinedSliderRendererComponent(obj)
			)
			return slider
		}

		fun createColourSliderAt(
			name: String,
			origin: Vec2,
			size: Vec2,
			shader: RenderShader,
			colours: MutableMap<String, Float>,
			action: ActionSliderComponent<Float>.() -> Unit = {}
		): ActionSliderComponent<Float> {
			val obj = GameObject(name, 1)
			obj.os(origin, size)
			return addColourSlider(obj, shader, colours, action)
		}

		fun createColourSliderAtPixel(
			name: String,
			pos: Vec2i,
			size: Vec2i,
			origin: Vec2,
			shader: RenderShader,
			colours: MutableMap<String, Float>,
			action: ActionSliderComponent<Float>.() -> Unit = {}
		): ActionSliderComponent<Float> {
			val obj = GameObject(name, 1)
			obj.pixel(pos, size, origin)
			return addColourSlider(obj, shader, colours, action)
		}

		fun addColourSlider(obj: GameObject, shader: RenderShader, colours: MutableMap<String, Float>, action: ActionSliderComponent<Float>.() -> Unit): ActionSliderComponent<Float> {
			val slider = ActionFloatSliderComponent(obj, 0f, 255f, 255f, action)
			obj.components.addAll(slider, ColourSliderRendererComponent(obj, shader, colours))
			return slider
		}
	}
}