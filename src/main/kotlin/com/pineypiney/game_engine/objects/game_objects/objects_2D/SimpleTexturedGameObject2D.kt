package com.pineypiney.game_engine.objects.game_objects.objects_2D

import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey

open class SimpleTexturedGameObject2D(override var texture: Texture, val shape: VertexShape = VertexShape.centerSquareShape, shader: Shader = defaultShader) : TexturedGameObject2D(shader) {

    constructor(textureKey: ResourceKey, shape: VertexShape = VertexShape.centerSquareShape, shader: Shader = defaultShader):
            this(TextureLoader.getTexture(textureKey), shape, shader)

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        super.render(renderer, tickDelta)

        texture.bind()

        shape.bindAndDraw()
    }
}