package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class StretchyStaticText(text: String, final override val window: WindowI, colour: Vec4 = Vec4(1, 1, 1, 1),
                              textMaxWidth: Float = 2f, textMaxHeight: Float = 2f,
                              font: Font = Font.defaultFont,
                              shader: Shader = font.shader):
    StretchyText(text, colour, textMaxWidth, textMaxHeight, font, shader), StaticTextI {

    constructor(text: String, window: WindowI, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                font: Font = Font.defaultFont,
                shader: Shader = Font.fontShader):
            this(text, window, colour, bounds.x, bounds.y, font, shader)

    override var origin: Vec2 = Vec2()

    init {
        defaultCharHeight = textMaxHeight
    }

    override fun init(){
        super.init()
        updateAspectRatio(window)
    }

    final override fun getScreenSize(): Vec2 = getScreenSize(text)

    override fun draw() {

        shader.use()
        shader.setUniforms(uniforms)
        val lineModel = I.translate(origin.x, origin.y, 0f).scale(defaultCharHeight / window.aspectRatio, defaultCharHeight, 1f)

        for(i in text.indices){

            val quad = quads[i]
            setIndividualUniforms(shader, quad)


            val model = lineModel.translate(Vec3(quad.offset, 0))
            shader.setMat4("model", model)

            quad.bindAndDraw()
        }


        if(underlineThickness > 0){
            drawUnderline(lineModel.translate(Vec3(quads[0].offset, 0)), text, underlineAmount)
        }
    }

    final override fun updateAspectRatio(window: WindowI) {

        // First scale the text so it touches the vertical bounds
        setDefaults(maxHeight)

        // Then, if it extends beyond the max width, scale it back down
        val widthRatio = getScreenSize().x/maxWidth
        if(widthRatio > 1){
            setDefaults(maxHeight/widthRatio)
        }
    }
}