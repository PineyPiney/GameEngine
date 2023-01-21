package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey
import glm_.mat4x4.Mat4
import glm_.vec4.Vec4

class CollisionBoxRenderer(val collider: CollisionBox2D, val parent: GameObject, override val shader: Shader): Renderable {

    override var visible: Boolean = true
    val shape = Shape.cornerSquareShape2D

    override val uniforms: Uniforms = shader.compileUniforms()

    override fun setUniforms() {
        uniforms.setMat4Uniform("model"){ parent.transform.model * collider.relModel }
        uniforms.setVec4Uniform("colour"){ Vec4(1) }
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double){
        super.render(view, projection, tickDelta)

        shape.bindAndDraw()
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))
    }
}