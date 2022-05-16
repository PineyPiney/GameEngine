package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Visual
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.text.Font
import glm_.vec4.Vec4

abstract class Text(override val text: String, override var colour: Vec4 = Vec4(1, 1, 1, 1),
                override val maxWidth: Float = 2f, override val maxHeight: Float = 2f,
                override val font: Font = Font.defaultFont,
                override val shader: Shader = font.shader, override val window: Window = Window.INSTANCE
): TextI, Visual {

    override var visible: Boolean = true

    final override var defaultCharWidth: Float = 0f
    final override var defaultCharHeight: Float = 0f

    final override fun getPixelWidth(text: String): Int{
        // Starting at 2 accounts for the margin at the beginning of the text
        var l = 2
        text.indices.forEach { l += (getCharWidth(text[it]) + font.characterSpacing) }
        return l
    }

    override fun toString(): String {
        return "Text[\"$text\"]"
    }

    companion object{
        val gameTextShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/text"))
    }
}