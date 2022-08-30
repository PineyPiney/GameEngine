package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2
import glm_.vec2.Vec2t

class Vec2iUniform(name: String, default: Vec2t<*> = Vec2(), getter: () -> Vec2t<*>? = { Vec2() }): Uniform<Vec2t<*>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec2i(name, value)
    }
}