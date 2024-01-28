package com.pineypiney.game_engine.level_editor.objects.util

import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.mat4x4.Mat4

open class ShadedGameItem(override var name: String, shader: Shader = defaultShader, val shape: VertexShape = VertexShape.centerSquareShape2D) : RenderedGameObject2D(shader){

    override fun init() {
        super.init()
        setUniforms()
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {

        shader.use()
        shader.setUniforms(uniforms)
        shader.setMat4("view", view)
        shader.setMat4("projection", projection)


        shape.bind()
        shape.draw()
    }

    override fun toString(): String{
        return "ShadedGameItem[Shader: ${shader}]"
    }
}