package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.text.Font
import glm_.f
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class SizedStaticText(text: String, fontSize: Int = 100, colour: Vec4 = Vec4(1, 1, 1, 1),
                           maxWidth: Float = 2f, maxHeight: Float = 2f,
                           separation: Float = 0.6f, font: Font = Font.defaultFont,
                           shader: Shader = font.shader, window: Window = Window.INSTANCE):
    SizedText(text, fontSize, colour, maxWidth, maxHeight, separation, font, shader, window), StaticTextI {

    constructor(text: String, fontSize: Int, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                separation: Float = 0.6f, font: Font = Font.defaultFont,
                shader: Shader = Font.fontShader, window: Window = Window.INSTANCE):
            this(text, fontSize, colour, bounds.x, bounds.y, separation, font, shader, window)

    constructor(text: String, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                separation: Float = 0.6f, font: Font = Font.defaultFont,
                shader: Shader = Font.fontShader, window: Window = Window.INSTANCE):
            this(text, 100, colour, bounds.x, bounds.y, separation, font, shader, window)

    override var origin: Vec2 = Vec2()
    final override val size: Vec2 = Vec2()

    init{
        setDefaults(fontSize.f / 100)
        updateAspectRatio(Window.INSTANCE)
    }

    final override fun setDefaults(height: Float){
        defaultCharHeight = height
        defaultCharWidth = height * 0.5f / window.aspectRatio
    }

    override fun getScreenSize(): Vec2 {
        if(lengths.isEmpty()) return Vec2()
        val maxWidth = lengths.maxOf { it }
        val height = defaultCharHeight * (1 + (separation * (lengths.size - 1)))
        return Vec2(maxWidth, height)
    }

    override fun draw() {

        val shader = shader
        shader.use()
        setUniversalUniforms(shader)
        font.texture.bind()

        var yOffset = separation * (lines.size - 1)

        val originModel = I.translate(Vec3(origin))

        var i = 0
        for(line in lines){

            // Add a bit of space at the beginning
            var xOffset = font.characterSpacing.f

            for(j in line.indices){

                setIndividualUniforms(shader, i)

                val charWidth = getCharWidth(line[j]).f
                quads[i].bind()

                var model = originModel.scale(Vec3(defaultCharWidth * (charWidth/font.letterWidth), defaultCharHeight, 1))
                model = model.translate(Vec3(xOffset  / charWidth, yOffset, 0))
                shader.setMat4("model", model)

                quads[i].draw()
                xOffset += (charWidth + font.characterSpacing)
                i++
            }

            yOffset -= 0.6f
        }
    }

    final override fun updateAspectRatio(window: Window) {
        lines = generateLines(window)
        lengths = lines.map { getScreenSize(it).x }.toFloatArray()
    }
}