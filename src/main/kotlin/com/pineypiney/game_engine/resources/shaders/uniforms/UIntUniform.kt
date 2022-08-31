package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader

class UIntUniform(name: String, default: UInt = 0u, getter: () -> UInt? = { 0u }): Uniform<UInt>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setUInt(name, value)
    }
}