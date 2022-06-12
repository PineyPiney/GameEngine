package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader

class IntUniform(name: String, default: Int = 0, getter: () -> Int? = {0}): Uniform<Int>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setInt(name, value)
    }
}