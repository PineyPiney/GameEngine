package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader

class IntsUniform(name: String, default: IntArray = intArrayOf(), getter: () -> IntArray? = { intArrayOf()}): Uniform<IntArray>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setInts(name, value)
    }
}