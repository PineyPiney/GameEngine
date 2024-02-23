package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class BoolsUniform(name: String, default: BooleanArray = booleanArrayOf(), getter: UniformGetter<BooleanArray> = { booleanArrayOf() }): Uniform<BooleanArray>(name, default, getter) {

    override fun apply(shader: Shader, renderer: RendererI<*>) {
        shader.setBools(name, getValue(renderer))
    }
}