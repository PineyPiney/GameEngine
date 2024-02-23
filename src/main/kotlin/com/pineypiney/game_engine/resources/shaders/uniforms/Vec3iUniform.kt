package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec3.Vec3
import glm_.vec3.Vec3t

class Vec3iUniform(name: String, default: Vec3t<*> = Vec3(), getter: (RendererI<*>) -> Vec3t<*>? = {Vec3()}): Uniform<Vec3t<*>>(name, default, getter) {

    override fun apply(shader: Shader, renderer: RendererI<*>) {
        shader.setVec3i(name, getValue(renderer))
    }
}