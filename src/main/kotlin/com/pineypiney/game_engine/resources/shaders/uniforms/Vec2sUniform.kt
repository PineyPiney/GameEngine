package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2

class Vec2sUniform(name: String, default: Array<Vec2> = arrayOf(), getter: () -> Array<Vec2>? = {arrayOf()}): Uniform<Array<Vec2>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec2s(name, value)
    }
}