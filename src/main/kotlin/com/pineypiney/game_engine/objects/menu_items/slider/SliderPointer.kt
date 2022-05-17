package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

abstract class SliderPointer(override val size: Vec2): InteractableMenuItem() {

    abstract val parent: Slider
    override val shader: Shader = transparentTextureShader

    override fun draw() {
        pointerTexture.bind()
        super.draw()
    }

    override fun onDrag(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2) {
        super.onDrag(game, cursorPos, cursorDelta)

        parent.moveSliderTo(cursorPos.x)
    }

    companion object{
        val pointerTexture = TextureLoader.getTexture(ResourceKey("menu_items/slider/pointer"))
    }
}