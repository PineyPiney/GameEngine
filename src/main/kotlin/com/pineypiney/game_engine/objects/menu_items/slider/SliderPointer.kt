package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

abstract class SliderPointer(val parent: Slider, val height: Float): InteractableMenuItem() {

    override var shader: Shader = transparentTextureShader

    override var size: Vec2 = calculateSize()

    override fun draw() {
        pointerTexture.bind()
        super.draw()
    }

    override fun onDrag(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2) {
        super.onDrag(game, cursorPos, cursorDelta)

        parent.moveSliderTo(cursorPos.x)
    }

    fun calculateSize() = Vec2(pointerTexture.aspectRatio / parent.window.aspectRatio, 1) * height * parent.size.y

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
        size = calculateSize()
    }

    companion object{
        val pointerTexture = TextureLoader.getTexture(ResourceKey("menu_items/slider/pointer"))
    }
}