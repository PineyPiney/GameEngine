package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec4.Vec4t

class Vec4sUniform(name: String, default: List<Vec4t<*>> = listOf(), getter: () -> List<Vec4t<*>>? = { listOf() }): Uniform<List<Vec4t<*>>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec4s(name, value)
    }
}