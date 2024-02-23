package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader

class UIntUniform(name: String, default: UInt = 0u, getter: (RendererI<*>) -> UInt? = { 0u }): Uniform<UInt>(name, default, getter) {

    override fun apply(shader: Shader, renderer: RendererI<*>) {
        shader.setUInt(name, getValue(renderer))
    }
}