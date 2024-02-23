package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.game_objects.transforms.Transform3D
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
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

    override val transform: Transform3D = Transform3D()

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        if(lines.isEmpty()) return

        val originModel = transform.model
        val totalWidth = lines.maxOf { getWidth(it.trim()) }

        var i = 0
        for(line in lines){
            super<SizedText>.render(renderer, tickDelta)

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
                    renderUnderline(lineModel.translate(Vec3(quads[firstIndex].offset, 0)), renderer.view, renderer.projection, displayLine, length)
                }
            }

            i += line.length
        }
    }

    override fun updateLines() {
        lines = generateLines()
        lengths = lines.map { font.getWidth(it) / 100f }.toFloatArray()
        super.updateLines()
    }
}