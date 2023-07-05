package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import glm_.vec4.Vec4

abstract class StretchyText(text: String, colour: Vec4 = Vec4(1, 1, 1, 1),
                            textMaxWidth: Float = 2f, textMaxHeight: Float = 2f,
                            font: Font = Font.defaultFont,
                            shader: Shader = gameTextShader):
    Text(text, colour, textMaxWidth, textMaxHeight, font, shader), StretchyTextI {

    // dimensions is Vec4(0, min y, pixel width, max y). min y and max y are used so that all letters are lined up vertically
    private var dimensions = Vec4()

    override var quads: Array<TextQuad> = arrayOf()

    override fun init() {
        super.init()
        quads = font.getQuads(text).toTypedArray()
    }
}