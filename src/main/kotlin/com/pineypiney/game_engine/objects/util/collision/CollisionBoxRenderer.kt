package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.vec4.Vec4

class CollisionBoxRenderer(val collider: CollisionBox, override val shader: Shader, override val uniforms: Uniforms): Renderable, Shaded {

    override var visible: Boolean = true
    val shape = Shape.cornerSquareShape2D

    override fun setUniforms() {
        uniforms.setMat4Uniform("model"){ (collider.parent?.transform?.model ?: I) * collider.relModel }
        uniforms.setVec4Uniform("colour"){ Vec4(1) }
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double){

        shader.use()
        shader.setUniforms(uniforms)
        shader.setMat4("projection", projection)
        shader.setMat4("view", view)

        shape.bind()
        shape.draw()
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))
    }
}