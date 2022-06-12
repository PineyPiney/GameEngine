package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec3.Vec3

class Vec3sUniform(name: String, default: Array<Vec3> = arrayOf(), getter: () -> Array<Vec3>? = {arrayOf()}): Uniform<Array<Vec3>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec3s(name, value)
    }
}