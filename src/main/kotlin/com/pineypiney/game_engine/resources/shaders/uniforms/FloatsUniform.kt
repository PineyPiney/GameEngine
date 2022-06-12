package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader

class FloatsUniform(name: String, default: FloatArray = floatArrayOf(), getter: () -> FloatArray? = { floatArrayOf()}): Uniform<FloatArray>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setFloats(name, value)
    }
}