package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader

class FloatUniform(name: String, default: Float = 0f, getter: () -> Float? = {0f}): Uniform<Float>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setFloat(name, value)
    }
}