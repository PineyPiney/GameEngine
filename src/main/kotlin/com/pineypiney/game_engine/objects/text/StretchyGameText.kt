package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class StretchyGameText(text: String, colour: Vec4 = Vec4(1, 1, 1, 1),
                            textMaxWidth: Float = 2f, textMaxHeight: Float = 2f,
                            font: Font = Font.defaultFont,
                            shader: Shader = gameTextShader):
    StretchyText(text, colour, textMaxWidth, textMaxHeight, font, shader), GameTextI {

    constructor(text: String, bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                font: Font = Font.defaultFont,
                shader: Shader = gameTextShader):
            this(text, colour, bounds.x, bounds.y, font, shader)

    override val transform: Transform2D = Transform2D()

    override fun init() {
        super.init()

        val size = font.getSize(text)
        val widthRatio = size.x * (maxHeight / maxWidth)

        if(widthRatio > 1){
            setDefaults(maxHeight/widthRatio)
        }
        else{
            setDefaults(maxHeight)
        }
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        super.render(view, projection, tickDelta)
        val lineModel = transform.model.scale(defaultCharHeight, defaultCharHeight, 1f)

        for(i in text.indices){

            val quad = quads[i]
            setIndividualUniforms(shader, quad)


            val model = lineModel.translate(Vec3(quad.offset, 0))
            shader.setMat4("model", model)

            quad.bindAndDraw()
        }

        if(underlineThickness > 0){
            renderUnderline(lineModel.translate(Vec3(quads[0].offset, 0)), view, projection)
        }
    }
}