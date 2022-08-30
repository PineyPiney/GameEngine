package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.BitMapFont
import com.pineypiney.game_engine.util.maths.I
import glm_.f
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class StretchyStaticText(text: String, final override val window: Window, colour: Vec4 = Vec4(1, 1, 1, 1),
                              textMaxWidth: Float = 2f, textMaxHeight: Float = 2f,
                              font: BitMapFont = BitMapFont.defaultFont,
                              shader: Shader = font.shader):
    StretchyText(text, colour, textMaxWidth, textMaxHeight, font, shader), StaticTextI {

    constructor(text: String, window: Window, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                font: BitMapFont = BitMapFont.defaultFont,
                shader: Shader = BitMapFont.fontShader):
            this(text, window, colour, bounds.x, bounds.y, font, shader)

    override var origin: Vec2 = Vec2()

    init {
        defaultCharHeight = textMaxHeight
    }

    override fun init(){
        super.init()
        updateAspectRatio(window)
    }

    final override fun setDefaults(height: Float){
        defaultCharHeight = height
        defaultCharWidth = height * (font.letterWidth / pixelHeight) / window.aspectRatio
    }

    override fun getScreenSize(): Vec2 = getScreenSize(text)

    override fun draw() {

        shader.use()
        shader.setUniforms(uniforms)
        font.texture.bind()

        // Add a bit of space at the beginning
        var xOffset = font.characterSpacing.f

        for(i in text.indices){

            setIndividualUniforms(shader, i)

            val charWidth = getCharWidth(text[i]).f
            quads[i].bind()

            var model = glm.translate(I, Vec3(origin))
            model = model.scale(Vec3(defaultCharWidth * (charWidth/font.letterWidth), defaultCharHeight, 1))
            model = model.translate(Vec3(xOffset  / charWidth, 0, 0))
            shader.setMat4("model", model)

            quads[i].draw()
            xOffset += (charWidth + font.characterSpacing)
        }
    }

    final override fun updateAspectRatio(window: Window) {

        // First scale the text so it touches the vertical bounds
        setDefaults(maxHeight)

        // Then, if it extends beyond the max width, scale it back down
        val widthRatio = getScreenSize().x/maxWidth
        if(widthRatio > 1){
            setDefaults(maxHeight/widthRatio)
        }
    }
}