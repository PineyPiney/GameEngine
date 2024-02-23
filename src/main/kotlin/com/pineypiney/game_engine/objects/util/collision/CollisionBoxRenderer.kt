package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.components.ColliderComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.rotate
import com.pineypiney.game_engine.util.maths.I
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class CollisionBoxRenderer(val collider: ColliderComponent, val colour: Vec4 = Vec4(1f), override val shader: Shader = defaultShader): Renderable {

    override var visible: Boolean = true
    val shape = VertexShape.cornerSquareShape

    override val uniforms: Uniforms = shader.compileUniforms()

    override fun setUniforms() {
        uniforms.setMat4Uniform("model"){ collider.transformedBox.run { I.translate(Vec3(origin, 0f)).rotate(angle).scale(Vec3(size, 1f)) } }
        uniforms.setVec4Uniform("colour", ::colour)
    }

    override fun render(renderer: RendererI<*>, tickDelta: Double){
        super.render(renderer, tickDelta)

        shape.bindAndDraw()
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))
    }
}