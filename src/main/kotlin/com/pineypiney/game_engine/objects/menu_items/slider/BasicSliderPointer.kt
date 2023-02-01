package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

open class BasicSliderPointer(override val parent: Slider, val height: Float): SliderPointer() {

    // Make size a variable to that it can be scaled when the aspect ratio is changed
    override var size: Vec2 = super.size
    override var shader: Shader = transparentTextureShader

    override fun init() {
        super.init()
        size = calculateSize()
    }

    override fun draw() {
        pointerTexture.bind()
        super.draw()
    }


    fun calculateSize() = Vec2(pointerTexture.aspectRatio / parent.window.aspectRatio, 1) * height * parent.size.y

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        size = calculateSize()
    }

    companion object{
        val pointerTexture = TextureLoader.getTexture(ResourceKey("menu_items/slider/pointer"))
    }
}