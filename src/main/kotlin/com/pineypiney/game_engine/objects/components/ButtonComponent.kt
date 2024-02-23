package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.fromHex
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class ButtonComponent(parent: GameObject, val action: (button: ButtonComponent) -> Unit): InteractorComponent(parent, "BTN"){

    var active: Boolean = true

    override val fields: Array<Field<*>> = super.fields + BooleanField("act", ::active) { active = it }

    var baseColour = Vec4.fromHex (0x00BFFF)
    var hoverColour = Vec4.fromHex(0x008CFF)
    var clickColour = Vec4.fromHex(0x026FFF)

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        val h = super.checkHover(ray, screenPos)
        parent.getComponent<ColourRendererComponent>()?.let {
            it.colour = when{
                pressed -> clickColour
                hover -> hoverColour
                else -> baseColour
            }
        }
        return h
    }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val ret = super.onPrimary(window, action, mods, cursorPos)
        if(ret == GLFW.GLFW_RELEASE && active){
            action(this)
        }
        return ret
    }
}