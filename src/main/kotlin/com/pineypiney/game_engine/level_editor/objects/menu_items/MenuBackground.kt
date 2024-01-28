package com.pineypiney.game_engine.level_editor.objects.menu_items

import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey

class MenuBackground(val texture: Texture): MenuItem() {

    constructor(name: String): this(TextureLoader.getTexture(ResourceKey(name)))

    override val shape: VertexShape = VertexShape.screenQuadShape
    override var shader: Shader = opaqueTextureShader

    override fun draw() {
        texture.bind()
        super.draw()
    }
}