package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.IGameLogic
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

abstract class AbstractButton : StaticInteractableMenuItem() {

    open var baseColour = Vec3(0x00, 0xBF, 0xFF) / 255
    open var hoverColour = Vec3(0x00, 0x8C, 0xFF) / 255
    open var clickColour = Vec3(0x02, 0x6F, 0xFF) / 255

    var active: Boolean = true
    abstract val action: (button: AbstractButton) -> Unit

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec3Uniform("colour", this::getCurrentColour)
    }

    override fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val ret = super.onPrimary(game, action, mods, cursorPos)
        if(ret == GLFW.GLFW_RELEASE && this.active){
            this.action.invoke(this)
        }
        return ret
    }

    private fun getCurrentColour() : Vec3 {
        return when{
            pressed -> clickColour
            hover -> hoverColour
            else -> baseColour
        }
    }
}