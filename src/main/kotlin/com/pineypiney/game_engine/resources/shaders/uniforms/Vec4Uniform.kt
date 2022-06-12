package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec4.Vec4

class Vec4Uniform(name: String, default: Vec4 = Vec4(), getter: () -> Vec4? = {Vec4()}): Uniform<Vec4>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec4(name, value)
    }
}