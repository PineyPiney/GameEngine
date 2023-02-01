package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.objects.text.StretchyStaticText
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.GLFunc
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class ScrollerText(text: String, window: WindowI,
                   private var limits: Vec2,
                   bounds: Vec2 = Vec2(2, 2), colour: Vec4 = Vec4(1, 1, 1, 1),
                   font: Font = Font.defaultFont,
                   shader: Shader = ScrollingListEntry.entryTextShader
): StretchyStaticText(text, window, bounds, colour, font, shader) {

    override fun setUniforms() {
        super.setUniforms()
        // Limit is in 0 to Window#height space so must be transformed
        uniforms.setVec2Uniform("limits"){ (limits + Vec2(1)) * (GLFunc.viewportO.y / 2f) }
    }

    override fun drawUnderline(model: Mat4, line: String, amount: Float) {
        val pos = origin.y + defaultCharHeight * underlineThickness * underlineOffset
        if(pos > limits.x && pos < limits.y){
            super.drawUnderline(model, line, amount)
        }
    }
}