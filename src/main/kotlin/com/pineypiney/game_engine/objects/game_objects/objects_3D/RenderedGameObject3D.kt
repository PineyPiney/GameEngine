package com.pineypiney.game_engine.objects.game_objects.objects_3D

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey

abstract class RenderedGameObject3D(shader: Shader): GameObject3D(), Renderable {

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
        uniforms.setMat4Uniform("model"){ worldModel }
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex\\3D"), ResourceKey("fragment\\texture"))
    }
}