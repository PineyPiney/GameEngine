package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import kool.ByteBuffer
import org.lwjgl.opengl.GL11C

abstract class OutlinedSlider(): Slider() {

    override var shader: Shader = translucentColourShader

    open var colour: Vec4 = Vec4(0.7)
    open var outlineThickness: Float = 0.01f
    open var outlineColour: Vec4 = Vec4(0.5, 0.5, 0.5, 1)

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setVec4Uniform("colour"){ colour }
    }

    override fun draw() {
        shader.use()
        shader.setUniforms(uniforms)
        // This only updates the stencil if the pixel passes all tests
        GLFunc.stencilTest = true
        GLFunc.stencilOp = Vec3i(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_REPLACE)
        GLFunc.stencilFRM = Vec3i(GL11C.GL_ALWAYS, 1, 0xFF)
        GLFunc.stencilWriteMask = 0xFF
        shape.bindAndDraw()

        val array = ByteBuffer(GLFunc.viewport.z * GLFunc.viewport.w)
        GL11C.glReadPixels(0, 0, GLFunc.viewport.z, GLFunc.viewport.w, GL11C.GL_STENCIL_INDEX, GL11C.GL_UNSIGNED_BYTE, array)

        GLFunc.stencilFRM = Vec3i(GL11C.GL_NOTEQUAL, 1, 0xFF)
        GLFunc.stencilWriteMask = 0

        val outline = Vec2(outlineThickness / window.aspectRatio, outlineThickness)
        shader.setMat4("model", model.translate(Vec3(-outline / size)).scale(Vec3((size + (outline * 2)) / size)))
        shader.setVec4("colour", outlineColour)
        shape.draw()

        GLFunc.stencilTest = false

        drawPointer()
    }

    companion object {
        val sliderShader = ShaderLoader.getShader(ResourceKey("vertex/2D_pass_pos"), ResourceKey("fragment/sliders/outlined_slider"))
    }
}