package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform3D
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import glm_.mat4x4.Mat4

interface GameTextI: TextI {

    val transform: Transform3D

    override fun getWidth(text: String): Float {
        val size = font.getWidth(text)
        return size * defaultCharHeight
    }

    fun renderUnderline(model: Mat4, view: Mat4, projection: Mat4, line: String = text, amount: Float = underlineAmount){
        val shader = RenderedGameObject2D.colourShader
        val newModel = model
            .scale(getWidth(line) * amount / defaultCharHeight, underlineThickness, 0f)
            .translate(0f, underlineOffset, 0f)

        shader.use()
        shader.setMat4("model", newModel)
        shader.setMat4("view", view)
        shader.setMat4("projection", projection)
        shader.setVec4("colour", colour)
        VertexShape.cornerSquareShape.bindAndDraw()
    }
}