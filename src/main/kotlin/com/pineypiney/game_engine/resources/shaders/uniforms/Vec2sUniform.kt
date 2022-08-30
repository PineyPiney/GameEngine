package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2t

class Vec2sUniform(name: String, default: Array<Vec2t<*>> = arrayOf(), getter: () -> Array<Vec2t<*>>? = {arrayOf()}): Uniform<Array<Vec2t<*>>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec2s(name, value)
    }
}