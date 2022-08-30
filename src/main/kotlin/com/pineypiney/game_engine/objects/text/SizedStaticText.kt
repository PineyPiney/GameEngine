package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.BitMapFont
import com.pineypiney.game_engine.util.maths.I
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class SizedStaticText(text: String, final override val window: Window, fontSize: Number = 100, colour: Vec4 = Vec4(1, 1, 1, 1),
                           maxWidth: Float = 2f, maxHeight: Float = 2f,
                           separation: Float = 0.6f, font: BitMapFont = BitMapFont.defaultFont,
                           shader: Shader = font.shader):
    SizedText(text, fontSize.i, colour, maxWidth, maxHeight, separation, font, shader), StaticTextI {

    constructor(text: String, window: Window, fontSize: Number, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                separation: Float = 0.6f, font: BitMapFont = BitMapFont.defaultFont,
                shader: Shader = BitMapFont.fontShader):
            this(text, window, fontSize, colour, bounds.x, bounds.y, separation, font, shader)

    constructor(text: String, window: Window, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                separation: Float = 0.6f, font: BitMapFont = BitMapFont.defaultFont,
                shader: Shader = BitMapFont.fontShader):
            this(text, window, 100, colour, bounds.x, bounds.y, separation, font, shader)

    override var origin: Vec2 = Vec2()
    final override val size: Vec2 = Vec2()

    init{
        setDefaults(fontSize.f / 100)
        updateAspectRatio(window)
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
        shader.setUniforms(uniforms)
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

    override fun updateLines() {
        lines = generateLines()
        lengths = lines.map { getScreenSize(it).x }.toFloatArray()
    }

    final override fun updateAspectRatio(window: Window) {
        setDefaults(fontSize.f / 100)
        updateLines()
    }
}