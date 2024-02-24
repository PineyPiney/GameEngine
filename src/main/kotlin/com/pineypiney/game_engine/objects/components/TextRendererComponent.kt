package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

open class TextRendererComponent(parent: GameObject, val text: Text, shader: Shader): RenderedComponent(parent, shader) {

    override val renderSize: Vec2 get() = Vec2(text.getWidth(), text.getHeight())

    override val shape: Shape get() = Rect2D(Vec2(), renderSize)

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec4Uniform("colour", text::colour)
        uniforms.setFloatUniform("italic", text::italic)
    }

    override fun init() {
        super.init()
        text.init()
    }

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        if(text.textChanged) {
            text.updateLines(renderer, parent.transformComponent.worldScale.let { it.x / it.y })
            text.textChanged = false
        }
        if(text.lines.isEmpty()) return

        val parentAspect = parent.transformComponent.worldScale.let { it.y / it.x }
        val aspectRatio = if(shader.hasProj) parentAspect else parentAspect / renderer.aspectRatio

        val originModel = getFormattedOrigin(text.getWidth() * aspectRatio, text.getHeight())
        val totalWidth = text.getWidth()

        var i = 0
        for(line in text.lines){
            shader.use()
            shader.setUniforms(uniforms, renderer)

            val displayLine = line.trim()
            val alignmentOffset = text.getAlignment(displayLine, totalWidth) * aspectRatio
            val lineModel = originModel.translate(alignmentOffset, 0f, 0f).scale(text.size * aspectRatio, text.size, 1f)

            val firstIndex = i + line.indexOfFirst { it != ' ' }
            for(j in displayLine.indices){

                val quad = text.quads[firstIndex + j]
                text.setIndividualUniforms(shader, quad)


                val model = lineModel.translate(Vec3(quad.offset, 0))
                shader.setMat4("model", model)

                quad.bindAndDraw()
            }

            if(text.underlineThickness > 0 && text.underlineAmount > 0){
                val length = if(text.underlineAmount == 1f) 1f
                else text.getUnderlineOf(text.lines.indexOf(line))

                if(length > 0){
                    renderUnderline(lineModel.translate(Vec3(text.quads[firstIndex].offset, 0)), renderer, displayLine, length)
                }
            }

            i += line.length
        }
    }

    fun renderUnderline(model: Mat4, renderer: RendererI<*>, line: String = text.text, amount: Float = text.underlineAmount){
        val shader = if(shader.hasView) ColourRendererComponent.defaultShader else ColourRendererComponent.menuShader
        val newModel = model.scale(text.font.getWidth(line) * amount, text.underlineThickness, 0f).translate(0f, text.underlineOffset, 0f)

        shader.use()
        if(shader.hasView) shader.setVP(renderer)
        shader.setMat4("model", newModel)
        shader.setVec4("colour", text.colour)
        VertexShape.cornerSquareShape.bindAndDraw()
    }

    fun getFormattedOrigin(w: Float, h: Float): Mat4{
        val o = Mat4(parent.worldModel)
        val a = text.alignment
        val bw = 1f - w
        val bh = 1f - h

        if(text.alignment == Text.ALIGN_BOTTOM_LEFT) return o

        when(a and 0xf){
            Text.ALIGN_CENTER_H -> o.translateAssign(bw * 0.5f, 0f, 0f)
            Text.ALIGN_RIGHT -> o.translateAssign(bw, 0f, 0f)
        }
        when(a and 0xf0){
            Text.ALIGN_CENTER_V -> o.translateAssign(0f, bh * 0.5f, 0f)
            Text.ALIGN_TOP -> o.translateAssign(0f, bh, 0f)
        }

        return o
    }

    override fun updateAspectRatio(renderer: RendererI<*>) {
        super.updateAspectRatio(renderer)
        if(!shader.hasProj) text.updateLines(renderer, parent.parent?.transformComponent?.worldScale?.run { x / y } ?: 1f)
    }

    override fun delete() {
        super.delete()
        text.delete()
    }

    companion object{
        val gameTextShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/text")]
    }
}