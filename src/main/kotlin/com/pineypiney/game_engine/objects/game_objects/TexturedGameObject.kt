package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.mat4x4.Mat4
import org.lwjgl.opengl.GL46C.GL_TEXTURE0
import org.lwjgl.opengl.GL46C.glActiveTexture

open class TexturedGameObject(override val id: ResourceKey, var texture: Texture, val shape: ArrayShape = ArrayShape.centerSquareShape, override val shader: Shader = defaultShader) : GameObject() {

    constructor(id: ResourceKey, textureKey: ResourceKey, shape: ArrayShape = ArrayShape.centerSquareShape, shader: Shader = defaultShader):
            this(id, TextureLoader.getTexture(textureKey), shape, shader)

    open val collidable = false

    override fun init() {
        super.init()
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {

        this.shape.bind()
        shader.use()

        shader.setMat4("model", transform.model)
        shader.setMat4("vp", projection * view)

        glActiveTexture(GL_TEXTURE0)
        texture.bind()

        drawArrays(this.shape.size)
    }

    override fun copy(): TexturedGameObject {
        return this.clone() as TexturedGameObject
    }

    override fun delete() {
        objects.forEach { it.gameItems.remove(this) }
    }

    override fun toString(): String{
        return "GameItem[Shape: $shape, Shader: ${shader}]"
    }
}