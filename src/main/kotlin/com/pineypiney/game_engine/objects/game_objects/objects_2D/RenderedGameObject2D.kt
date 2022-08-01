package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey

abstract class RenderedGameObject2D(shader: Shader): GameObject2D(), Renderable, Shaded {

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

    abstract override fun copy(): RenderedGameObject2D

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex\\2D"), ResourceKey("fragment\\texture"))
    }
}