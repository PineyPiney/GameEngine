package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class DoublesUniform(name: String, default: DoubleArray = doubleArrayOf(), getter: (RendererI<*>) -> DoubleArray? = { doubleArrayOf() }): Uniform<DoubleArray>(name, default, getter) {

    override fun apply(shader: Shader, renderer: RendererI<*>) {
        shader.setDoubles(name, getValue(renderer))
    }
}