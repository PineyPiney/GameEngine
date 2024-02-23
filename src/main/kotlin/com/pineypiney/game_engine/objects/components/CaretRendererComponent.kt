package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class CaretRendererComponent(parent: GameObject, colour: Vec4, shader: Shader = defaultShader, shape: VertexShape = VertexShape.centerSquareShape): ColourRendererComponent(parent, colour, shader, shape) {

    val textField get() = parent.parent?.getComponent<TextFieldComponent>()!!

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        positionTextAndCaret(renderer)
        super.render(renderer, tickDelta)
    }

    fun positionTextAndCaret(renderer: RendererI<*>){
        val w = textField.textBox.getComponent<TextRendererComponent>()!!.text.getWidth(0..<textField.caret)
        val ar = (textField.parent.getComponent<RenderedComponent>()!!.renderSize * Vec2(textField.parent.transformComponent.worldScale)).run { y / (x * renderer.aspectRatio) }
        parent.position = Vec3(w * ar, 0f, 0f)
    }

    override fun updateAspectRatio(renderer: RendererI<*>) {
        super.updateAspectRatio(renderer)
        positionTextAndCaret(renderer)
    }
}