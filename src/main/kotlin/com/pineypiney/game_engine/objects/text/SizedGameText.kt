package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import glm_.f
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class SizedGameText(text: String, fontSize: Int = 100, colour: Vec4 = Vec4(1, 1, 1, 1),
                         maxWidth: Float = 2f, maxHeight: Float = 2f,
                         separation: Float = 0.6f,
                         font: Font = Font.defaultFont,
                         shader: Shader = gameTextShader):
    SizedText(text, fontSize, colour, maxWidth, maxHeight, separation, font, shader), GameTextI {

    constructor(text: String, fontSize: Int, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                separation: Float = 0.6f, font: Font = Font.defaultFont,
                shader: Shader = gameTextShader):
            this(text, fontSize, colour, bounds.x, bounds.y, separation, font, shader)

    override val transform: Transform2D = Transform2D()

    init {
        setDefaults(fontSize.f / 100)
    }

    final override fun setDefaults(height: Float) {
        defaultCharHeight = height * 0.5f
        updateLines()
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        if(lines.isEmpty()) return

        val originModel = transform.model
        val totalWidth = lines.maxOf { getWidth(it.trim()) }

        var i = 0
        for(line in lines){
            super.render(view, projection, tickDelta)

            val displayLine = line.trim()
            val firstIndex = i + line.indexOfFirst { it != ' ' }

            val alignmentOffset = getAlignment(displayLine, totalWidth)
            val lineModel = originModel.translate(alignmentOffset, 0f, 0f).scale(defaultCharHeight, defaultCharHeight, 1f)

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

                if(length > 0) {
                    renderUnderline(lineModel.translate(Vec3(quads[firstIndex].offset, 0)), view, projection, displayLine, length)
                }
            }

            i += line.length
        }
    }

    override fun updateLines() {
        lines = generateLines()
        lengths = lines.map { font.getSize(it).x / 100f }.toFloatArray()
    }
}