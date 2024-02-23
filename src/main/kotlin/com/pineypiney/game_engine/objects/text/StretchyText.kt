package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import glm_.vec4.Vec4

abstract class StretchyText(text: String, colour: Vec4 = Vec4(1, 1, 1, 1),
                            textMaxWidth: Float = 2f, textMaxHeight: Float = 2f,
                            font: Font = Font.defaultFont,
                            shader: Shader = gameTextShader):
    OldText(text, colour, textMaxWidth, textMaxHeight, font, shader), StretchyTextI {

    override var quads: Array<TextQuad> = arrayOf()

    override fun init() {
        super.init()
        quads = font.getQuads(text).toTypedArray()
    }
}