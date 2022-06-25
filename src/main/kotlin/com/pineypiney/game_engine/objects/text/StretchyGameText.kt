package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.game_objects.Transform
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import glm_.f
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

    final override val maxWidth: Float
        get() = super.maxWidth
    final override val maxHeight: Float
        get() = super.maxHeight

    override val transform: Transform = Transform()

    override fun init() {
        super.init()

        // First scale the text so it touches the vertical bounds
        setDefaults(maxHeight)

        // Then, if it extends beyond the max width, scale it back down
        val widthRatio = pixelToRelative(getPixelWidth(text))/maxWidth
        if(widthRatio > 1){
            setDefaults(maxHeight/widthRatio)
        }
    }

    final override fun pixelToRelative(pixel: Int): Float {
        return super.pixelToRelative(pixel)
    }

    final override fun setDefaults(height: Float){
        defaultCharHeight = height
        defaultCharWidth = height * (font.letterWidth / pixelHeight)
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        super.render(view, projection, tickDelta)

        font.texture.bind()

        // Add a bit of space at the beginning
        var xOffset = font.characterSpacing.f
        for(i in text.indices){

            setIndividualUniforms(shader, i)

            val charWidth = getCharWidth(text[i]).f
            quads[i].bind()

            var model = transform.model
            model = model.scale(Vec3(defaultCharWidth * (charWidth/font.letterWidth), defaultCharHeight, 1))
            model = model.translate(Vec3(xOffset  / charWidth, 0, 0))
            shader.setMat4("model", model)

            quads[i].draw()
            xOffset += (charWidth + font.characterSpacing)
        }
    }
}