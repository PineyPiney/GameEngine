package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec4.Vec4t

class Vec4isUniform(name: String, default: Array<Vec4t<*>> = arrayOf(), getter: () -> Array<Vec4t<*>>? = {arrayOf()}): Uniform<Array<Vec4t<*>>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec4is(name, value)
    }
}