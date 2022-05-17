package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.util.shapes.IndicesShape
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture

class MenuBackground(val texture: Texture, override val shader: Shader = menuTextureShader) : MenuItem(), Storable {

    override val shape: Shape = IndicesShape.screenQuadShape

    override fun init() {

    }

    override fun draw() {
        texture.bind()
        super.draw()
    }

    override fun addTo(objects: ScreenObjectCollection) {
        objects.backgrounds.add(this)
    }

    override fun removeFrom(objects: ScreenObjectCollection) {
        objects.backgrounds.add(this)
    }

    override fun delete() {
        if(shader != menuTextureShader) shader.delete()
    }

    override fun toString(): String{
        return "BackgroundItem[Texture: $texture, Shader: $shader]"
    }
}