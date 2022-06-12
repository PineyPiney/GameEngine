package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader

abstract class Uniform<E>(val name: String, val default: E, var getter: () -> E?) {

    val value; get() = getter() ?: default

    abstract fun apply(shader: Shader)
}