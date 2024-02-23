package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.components.slider.SliderPointerComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec3.Vec3

open class BasicSliderPointer(val height: Float): MenuItem() {

    override var name: String = "SliderPointer"

    override fun addComponents() {
        super.addComponents()
        components.add(SliderPointerComponent(this))
        components.add(object : SpriteComponent(this@BasicSliderPointer, pointerTexture, pointerTexture.height.toFloat() / height, transparentTextureShader){

            override fun updateAspectRatio(renderer: RendererI<*>) {
                super.updateAspectRatio(renderer)

                scale = Vec3(1f / (renderer.aspectRatio * (parent.parent?.scale?.run { x / y } ?: 1f)), 1f, 1f)
            }
        })
    }

    companion object{
        val pointerTexture = TextureLoader.getTexture(ResourceKey("menu_items/slider/pointer"))
    }
}