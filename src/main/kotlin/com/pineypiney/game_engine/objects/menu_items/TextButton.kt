package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.text.StretchyStaticText
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class TextButton(val string: String, override val origin: Vec2, final override val size: Vec2, window: WindowI, textColour: Vec4 = Vec4(0, 0, 0, 1), override val action: (button: AbstractButton) -> Unit) : AbstractButton() {

    private val text: StretchyStaticText = StretchyStaticText(string, window, size, textColour)
    private var textPos = Vec2()

    open var baseColour = Vec3.fromHex (0x00BFFF)
    open var hoverColour = Vec3.fromHex(0x008CFF)
    open var clickColour = Vec3.fromHex(0x026FFF)


    var textColour: Vec4
        get() = text.colour
        set(value) { text.colour = value }

    override fun init(){
        super.init()

        text.init()
        textPos = origin + (size * 0.5)
    }

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec3Uniform("colour", ::getCurrentColour)
    }

    override fun draw() {
        super.draw()
        text.drawCentered(textPos)
    }

    private fun getCurrentColour() : Vec3 {
        return when{
            pressed -> clickColour
            hover -> hoverColour
            else -> baseColour
        }
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        text.updateAspectRatio(window)
    }

    override fun delete() {
        super.delete()
        text.delete()
    }
}