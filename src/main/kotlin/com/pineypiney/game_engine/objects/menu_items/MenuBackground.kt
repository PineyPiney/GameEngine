package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.I

class MenuBackground(val texture: Texture, override val shader: Shader = menuTextureShader) : MenuItem(), Storable {

    override val shape: Shape = menuShape

    override fun init() {

    }

    override fun draw() {
        shape.bind()

        shader.use()
        shader.setMat4("model", I)

        texture.bind()

        shape.draw()
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