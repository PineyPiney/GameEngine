package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ButtonComponent
import com.pineypiney.game_engine.objects.components.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class SpriteButton(val icon: Texture, val ppu: Float, origin: Vec3 = Vec3(0f), size: Vec2 = Vec2(1f), val shader: Shader = SpriteComponent.menuShader, override val action: (button: ButtonComponent) -> Unit) : AbstractButton() {

    var baseTint = Vec4(1f)
    var hoverTint = Vec4(.95f)
    var clickTint = Vec4(.9f)

    init {
        os(origin, size)
    }

    override fun addComponents() {
        super.addComponents()
        components.add(ColouredSpriteComponent(this, icon, ppu, ::selectColour, shader, VertexShape.cornerSquareShape))
    }

    fun selectColour(): Vec4{
        return when{
            getComponent<ButtonComponent>()!!.pressed -> clickTint
            getComponent<ButtonComponent>()!!.hover -> hoverTint
            else -> baseTint
        }
    }
}