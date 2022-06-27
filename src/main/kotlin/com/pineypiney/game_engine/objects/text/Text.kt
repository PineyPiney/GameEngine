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

    final override var defaultCharWidth: Float = 0f
    final override var defaultCharHeight: Float = 0f

    override fun init() {
        uniforms = shader.compileUniforms()
    }

    override fun setUniforms() {
        uniforms.setVec4Uniform("colour"){ colour }
    }

    final override fun getPixelWidth(text: String): Int{
        // Starting at 2 accounts for the margin at the beginning of the text
        return font.characterSpacing + text.sumOf { getCharWidth(it) + font.characterSpacing }
    }

    override fun toString(): String {
        return "Text[\"$text\"]"
    }

    companion object{
        val gameTextShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/text"))
    }
}