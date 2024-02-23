package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec4.Vec4
import glm_.vec4.Vec4t

class Vec4iUniform(name: String, default: Vec4t<*> = Vec4(), getter: (RendererI<*>) -> Vec4t<*>? = {Vec4()}): Uniform<Vec4t<*>>(name, default, getter) {

    override fun apply(shader: Shader, renderer: RendererI<*>) {
        shader.setVec4i(name, getValue(renderer))
    }
}