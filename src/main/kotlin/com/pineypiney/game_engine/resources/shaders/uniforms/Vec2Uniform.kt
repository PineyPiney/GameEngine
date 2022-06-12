package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2

class Vec2Uniform(name: String, default: Vec2 = Vec2(), getter: () -> Vec2? = {Vec2()}): Uniform<Vec2>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec2(name, value)
    }
}