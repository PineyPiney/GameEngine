package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

abstract class Uniform<E : Any>(val name: String, val default: E, var getter: UniformGetter<E>) {

	abstract fun apply(shader: Shader, renderer: RendererI)

	fun getValue(renderer: RendererI) = getter(renderer) ?: default

	override fun toString(): String {
		return "Uniform<${default::class.simpleName}>[$name]"
	}
}

typealias UniformGetter<E> = (renderer: RendererI) -> E?