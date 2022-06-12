package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec4.Vec4

class Vec4sUniform(name: String, default: Array<Vec4> = arrayOf(), getter: () -> Array<Vec4>? = {arrayOf()}): Uniform<Array<Vec4>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec4s(name, value)
    }
}