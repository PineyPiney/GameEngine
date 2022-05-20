package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.text.StretchyStaticText
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.text.Font
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class ScrollerText(text: String, bounds: Vec2 = Vec2(2, 2),
                   private var limits: Vec2, colour: Vec4 = Vec4(1, 1, 1, 1),
                   font: Font = Font.defaultFont,
                   shader: Shader = ScrollingListEntry.entryTextShader, window: Window = Window.INSTANCE

): StretchyStaticText(text, bounds, colour, font, shader, window) {

    override fun setUniversalUniforms(shader: Shader) {
        super.setUniversalUniforms(shader)
        shader.setVec2("limits", limits)
    }

    override fun setIndividualUniforms(shader: Shader, index: Int) {
        super.setIndividualUniforms(shader, index)
        val q: TextQuad? = getQuad(index)
        if(q != null){
            val bottom = q.bottomRight.y
            // This Vec2 contains the bottom of the texture and the height
            shader.setVec2("texture_section", Vec2(bottom, q.topLeft.y - bottom))
        }
    }
}