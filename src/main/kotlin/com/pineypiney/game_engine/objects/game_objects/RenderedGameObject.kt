package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey

abstract class RenderedGameObject(shader: Shader): GameObject(), Renderable, Shaded {

    override var visible: Boolean = true

    override var shader: Shader = shader
        set(value) {
            field = value
            uniforms = field.compileUniforms()
        }
    override var uniforms: Uniforms = shader.compileUniforms()
        set(value) {
            field = value
            setUniforms()
        }

    override fun init() {
        super.init()
        uniforms = shader.compileUniforms()
    }

    override fun setUniforms() {
        uniforms.setMat4Uniform("model"){ transform.model }
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex\\2D"), ResourceKey("fragment\\texture"))
    }
}