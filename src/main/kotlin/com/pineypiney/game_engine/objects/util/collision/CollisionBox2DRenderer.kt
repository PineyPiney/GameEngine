package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.RenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.rotate
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class CollisionBox2DRenderer(val obj: GameObject, val colour: Vec4 = Vec4(1f), val shader: Shader = defaultShader): GameObject() {

    override fun addComponents() {
        super.addComponents()
        components.add(object : RenderedComponent(this@CollisionBox2DRenderer, shader) {

            val vShape = VertexShape.cornerSquareShape
            override val shape: Shape = vShape.shape
            override val renderSize: Vec2 = Vec2(1f)

            override fun setUniforms() {
                super.setUniforms()
                uniforms.setMat4Uniform("model"){ (this@CollisionBox2DRenderer.obj.getShape() as Rect2D).run { I.translate(Vec3(origin, 0f)).rotate(angle).scale(Vec3(size, 1f)) } }
                uniforms.setVec4Uniform("colour", ::colour)
            }

            override fun render(renderer: RendererI<*>, tickDelta: Double){
                shader.setUp(uniforms, renderer)
                vShape.bindAndDraw()
            }
        })
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))
    }
}