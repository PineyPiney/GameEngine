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
        defaultCharHeight = height
        defaultCharWidth = height * 0.5f
        updateLines()
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        super.render(view, projection, tickDelta)

        font.texture.bind()

        val originModel = transform.model

        var yOffset = separation * (lines.size - 1)
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
        lengths = lines.map { pixelToRelative(getPixelWidth(it)) }.toFloatArray()
    }
}