package com.pineypiney.game_engine.objects.game_objects.objects_3D

import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.mat4x4.Mat4

open class SimpleTexturedGameObject3D(override var texture: Texture, val shape: Shape = Shape.centerCubeShape, shader: Shader = defaultShader) : TexturedGameObject3D(shader), Shaded {

    constructor(textureKey: ResourceKey, shape: Shape = Shape.centerCubeShape, shader: Shader = defaultShader):
            this(TextureLoader.getTexture(textureKey), shape, shader)

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        super.render(view, projection, tickDelta)

        texture.bind()

        shape.bind()
        shape.draw()
    }
}