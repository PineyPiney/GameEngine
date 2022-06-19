package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.mat4x4.Mat4

open class SimpleTexturedGameObject(override val id: ResourceKey, override var texture: Texture, val shape: Shape = Shape.centerSquareShape2D, shader: Shader = defaultShader) : TexturedGameObject(shader), Shaded {

    constructor(id: ResourceKey, textureKey: ResourceKey, shape: Shape = Shape.centerSquareShape2D, shader: Shader = defaultShader):
            this(id, TextureLoader.getTexture(textureKey), shape, shader)

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        super.render(view, projection, tickDelta)

        texture.bind()

        shape.bind()
        shape.draw()
    }

    override fun copy(): SimpleTexturedGameObject {
        return SimpleTexturedGameObject(id, texture, shape, shader)
    }
}