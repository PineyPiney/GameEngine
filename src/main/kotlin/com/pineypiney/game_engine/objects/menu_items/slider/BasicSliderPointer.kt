package com.pineypiney.game_engine.objects.menu_items.slider

import com.pineypiney.game_engine.objects.components.SpriteComponent
import com.pineypiney.game_engine.objects.components.slider.SliderPointerComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec3.Vec3

open class BasicSliderPointer(val height: Float): MenuItem() {

    override var name: String = "SliderPointer"

    override fun addComponents() {
        super.addComponents()
        components.add(SliderPointerComponent(this))
        components.add(SpriteComponent(this@BasicSliderPointer, pointerTexture, pointerTexture.height.toFloat() / height, SpriteComponent.menuShader))
    }

    override fun init() {
        super.init()
        val parentHeight = parent?.transformComponent?.worldScale?.y ?: 1f
        this.transformComponent.worldScale = Vec3(parentHeight, parentHeight, 1f)
    }

    companion object{
        val pointerTexture = TextureLoader.getTexture(ResourceKey("menu_items/slider/pointer"))
    }
}