package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowI
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class SizedStaticText(text: String, final override val window: WindowI, fontSize: Number = 100, colour: Vec4 = Vec4(1, 1, 1, 1),
                           maxWidth: Float = 2f, maxHeight: Float = 2f,
                           separation: Float = 0.6f, font: Font = Font.defaultFont,
                           shader: Shader = font.shader):
    SizedText(text, fontSize.i, colour, maxWidth, maxHeight, separation, font, shader), StaticTextI {

    constructor(text: String, window: WindowI, fontSize: Number, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                separation: Float = 0.6f, font: Font = Font.defaultFont,
                shader: Shader = Font.fontShader):
            this(text, window, fontSize, colour, bounds.x, bounds.y, separation, font, shader)

    constructor(text: String, window: WindowI, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                separation: Float = 0.6f, font: Font = Font.defaultFont,
                shader: Shader = Font.fontShader):
            this(text, window, 100, colour, bounds.x, bounds.y, separation, font, shader)

    override var origin: Vec2 = Vec2()

    final override fun getScreenSize(): Vec2 {
        if(lengths.isEmpty()) return Vec2()
        val maxWidth = lengths.max()
        val height = getScreenHeight()
        return Vec2(maxWidth, height)
    }

    fun getScreenWidth(): Float = if(lengths.isEmpty()) 0f else lengths.max()
    fun getScreenHeight(): Float = if(lengths.isEmpty()) 0f else defaultCharHeight * font.getHeight(text)

    override fun draw() {
        if(lines.isEmpty()) return

        val originModel = I.translate(Vec3(origin))
        val totalWidth = lines.maxOf { getWidth(it.trim()) }

        var i = 0
        for(line in lines){
            shader.use()
            //shader.setUniforms(uniforms,) TODO

            val displayLine = line.trim()
            val alignmentOffset = getAlignment(displayLine, totalWidth)
            val lineModel = originModel.translate(alignmentOffset, 0f, 0f).scale(defaultCharHeight / window.aspectRatio, defaultCharHeight, 1f)

            val firstIndex = i + line.indexOfFirst { it != ' ' }
            for(j in displayLine.indices){

                val quad = quads[firstIndex + j]
                setIndividualUniforms(shader, quad)


                val model = lineModel.translate(Vec3(quad.offset, 0))
                shader.setMat4("model", model)

                quad.bindAndDraw()
            }

            if(underlineThickness > 0 && underlineAmount > 0){
                val length = if(underlineAmount == 1f) 1f
                else getUnderlineOf(lines.indexOf(line))

                if(length > 0){
                    drawUnderline(lineModel.translate(Vec3(quads[firstIndex].offset, 0)), displayLine, length)
                }
            }

            i += line.length
        }
    }

    override fun updateLines() {
        lines = generateLines()
        lengths = lines.map { getWidth(it) }.toFloatArray()
        super.updateLines()
    }

    final override fun updateAspectRatio(window: WindowI) {
        setDefaults(fontSize.f / 100)
    }
}