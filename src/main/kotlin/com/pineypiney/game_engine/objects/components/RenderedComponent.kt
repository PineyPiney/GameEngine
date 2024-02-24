package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.filterValueIsInstance
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.min

abstract class RenderedComponent(parent: GameObject, s: Shader): Component("RND", parent) {

    var visible = true
    abstract val renderSize: Vec2
    abstract val shape: Shape

    var shader: Shader = s
        set(value) {
            field = value
            uniforms = field.compileUniforms()
        }

    var uniforms: Uniforms = shader.compileUniforms()
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

    open fun setUniforms(){
        if(shader.hasView) uniforms.setMat4UniformR("view", RendererI<*>::view)
        if(shader.hasProj) uniforms.setMat4UniformR("projection", RendererI<*>::projection)
        if(shader.hasPort) uniforms.setVec2iUniformR("viewport", RendererI<*>::viewportSize)
        if(shader.hasPos) uniforms.setVec3UniformR("viewPos", RendererI<*>::viewPos)
        uniforms.setMat4Uniform("model", parent::worldModel)
    }

    abstract fun render(renderer: RendererI<*>, tickDelta: Double)

    open fun updateAspectRatio(renderer: RendererI<*>){}

    fun setLightUniforms(){
        val lights = (parent.objects ?: return).getAllComponents().filterIsInstance<LightComponent>().filter { it.light.on }
        lights.firstOrNull { it.light is DirectionalLight }?.setShaderUniforms(shader, "dirLight")
        val pointLights = lights.associate { it.parent.position to it.light }.filterValueIsInstance<Vec3, PointLight>().entries.sortedByDescending { (it.key - parent.position).length() / it.value.linear }
        for(l in 0..<min(4, pointLights.size)){
            val name = "pointLights[$l]"
            shader.setVec3("$name.position", pointLights[l].key)
            pointLights[l].value.setShaderUniforms(shader, name)
        }
        lights.firstOrNull { it.light is SpotLight }?.setShaderUniforms(shader, "spotlight")
    }

    companion object{

        val default2DShader = ShaderLoader.getShader(ResourceKey("vertex\\2D"), ResourceKey("fragment\\texture"))
        val default3DShader = ShaderLoader.getShader(ResourceKey("vertex\\3D"), ResourceKey("fragment\\texture"))
        val colourShader = ShaderLoader[ResourceKey("vertex\\2D"), ResourceKey("fragment\\colour")]
    }
}