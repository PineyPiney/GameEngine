package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey

abstract class RenderedComponent(parent: GameObject, s: Shader): Component(parent, "RND"), RenderedComponentI {

    final override var visible = true

    final override var shader: Shader = s
        set(value) {
            field = value
            uniforms = field.compileUniforms()
        }

    final override var uniforms: Uniforms = shader.compileUniforms()
        set(value) {
            field = value
            setUniforms()
        }

    override val fields: Array<Field<*>> = arrayOf(
        BooleanField("vsb", ::visible){ visible = it },
        Field("vsh", ::DefaultFieldEditor, shader::vName, { shader = ShaderLoader[ResourceKey(it), ResourceKey(shader.fName)] }, { it }, { _, s -> s }),
        Field("fsh", ::DefaultFieldEditor, shader::fName, { shader = ShaderLoader[ResourceKey(shader.vName), ResourceKey(it)] }, { it }, { _, s -> s })
    )

    override fun init() {
        super.init()
        setUniforms()
    }

    override fun setUniforms(){
        if(shader.hasView) uniforms.setMat4UniformR("view", RendererI<*>::view)
        if(shader.hasProj) uniforms.setMat4UniformR("projection", RendererI<*>::projection)
        if(shader.hasGUI) uniforms.setMat4UniformR("guiProjection", RendererI<*>::guiProjection)
        if(shader.hasPort) uniforms.setVec2iUniformR("viewport", RendererI<*>::viewportSize)
        if(shader.hasPos) uniforms.setVec3UniformR("viewPos", RendererI<*>::viewPos)
        uniforms.setMat4Uniform("model", parent::worldModel)
    }
}