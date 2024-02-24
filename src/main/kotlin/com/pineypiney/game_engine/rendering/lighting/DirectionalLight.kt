package com.pineypiney.game_engine.rendering.lighting

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec3.Vec3

class DirectionalLight(val direction: Vec3, override val ambient: Vec3 = Vec3(0.1f), override val diffuse: Vec3 = Vec3(0.5f), override val specular: Vec3 = Vec3(1f)): Light() {

    override fun setShaderUniforms(shader: Shader, name: String) {
        super.setShaderUniforms(shader, name)
        shader.setVec3("$name.direction", direction)
    }
}