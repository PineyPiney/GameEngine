package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.Visual
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec4.Vec4

abstract class Text(text: String, override var colour: Vec4 = Vec4(1, 1, 1, 1),
                    override val maxWidth: Float = 2f, override val maxHeight: Float = 2f,
                    override val font: Font = Font.defaultFont,
                    shader: Shader = font.shader): TextI, Visual {

    override var text: String = text
        set(value){
            if(field != value){
                delete()
                field = value
                init()
            }
        }

    override var visible: Boolean = true
    override var shader: Shader = shader
        set(value) {
            field = value
            uniforms = field.compileUniforms()
        }
    override var uniforms: Uniforms = Uniforms.default
        set(value) {
            field = value
            setUniforms()
        }

    override var italic: Float = 0f
    override var underlineThickness: Float = 0f
    override var underlineOffset: Float = -2f
    override var underlineAmount: Float = 1f
    final override var defaultCharHeight: Float = 0f

    override fun init() {
        uniforms = shader.compileUniforms()
    }

    override fun setUniforms() {
        uniforms.setVec4Uniform("colour"){ colour }
        uniforms.setFloatUniform("italic"){ italic }
    }

    override fun toString(): String {
        return "Text[\"$text\"]"
    }

    companion object{
        val gameTextShader = ShaderLoader.getShader(ResourceKey("vertex/text"), ResourceKey("fragment/text"))
    }
}