package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec3.Vec3

class Vec3Uniform(name: String, default: Vec3 = Vec3(), getter: () -> Vec3? = {Vec3()}): Uniform<Vec3>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setVec3(name, value)
    }
}