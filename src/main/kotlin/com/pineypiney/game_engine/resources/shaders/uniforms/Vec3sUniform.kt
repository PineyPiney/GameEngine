package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec3.Vec3t

class Vec3sUniform(name: String, default: List<Vec3t<*>> = listOf(), getter: () -> List<Vec3t<*>>? = {listOf()}): Uniform<List<Vec3t<*>>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec3s(name, value)
    }
}