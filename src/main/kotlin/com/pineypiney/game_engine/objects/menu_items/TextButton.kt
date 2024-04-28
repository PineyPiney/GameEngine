package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.ColourRendererComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.util.extension_functions.fromHex
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class TextButton(val string: String, origin: Vec2, size: Vec2, textColour: Vec4 = Vec4(0, 0, 0, 1), override val action: (button: ButtonComponent) -> Unit) : AbstractButton() {

    var textObject = Text.makeMenuText(string, textColour, 1f, 1f, 0f, Text.ALIGN_CENTER)

    var baseColour = Vec4.fromHex (0x00BFFF)
    var hoverColour = Vec4.fromHex(0x008CFF)
    var clickColour = Vec4.fromHex(0x026FFF)

    init {
        os(origin, size)
    }

    override fun addComponents() {
        super.addComponents()
        components.add(ColourRendererComponent(this, ::selectColour, ColourRendererComponent.menuShader, VertexShape.cornerSquareShape))
    }

    override fun addChildren() {
        super.addChildren()
        addChild(textObject)
        textObject.translate(Vec3(0f, 0f, .01f))
    }

    fun selectColour(): Vec4{
        return when{
            getComponent<ButtonComponent>()!!.pressed -> clickColour
            getComponent<ButtonComponent>()!!.hover -> hoverColour
            else -> baseColour
        }
    }
}