package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms

abstract class RenderedGameObject(shader: Shader): GameObject(), Renderable, Shaded {

    override var visible: Boolean = true

    final override var shader: Shader = shader
        set(value) {
            field = value
            uniforms = field.compileUniforms()
        }
    final override var uniforms: Uniforms = shader.compileUniforms()
        set(value) {
            field = value
            setUniforms()
        }

    override fun init() {
        super.init()
        setUniforms()
    }

    override fun setUniforms() {
        uniforms.setMat4Uniform("model"){ transform.model }

        println("Setting Uniforms")
        Error().printStackTrace()
    }
}