package com.pineypiney.game_engine.visual.game_objects

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.visual.util.collision.CollisionBox
import com.pineypiney.game_engine.visual.util.collision.SoftCollisionBox
import com.pineypiney.game_engine.visual.util.shapes.ArrayShape
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import org.lwjgl.opengl.GL46C.GL_TEXTURE0
import org.lwjgl.opengl.GL46C.glActiveTexture

open class TexturedGameObject(override val id: ResourceKey, var texture: Texture, val shape: ArrayShape = ArrayShape.centerSquareShape, override val shader: Shader = defaultShader) : GameObject() {

    constructor(id: ResourceKey, textureKey: ResourceKey, shape: ArrayShape = ArrayShape.centerSquareShape, shader: Shader = defaultShader):
            this(id, TextureLoader.getTexture(textureKey), shape, shader)

    open val collidable = false

    override val collision: CollisionBox = SoftCollisionBox(null, Vec2(-0.5), Vec2(1))

    override fun init() {
        super.init()
        collision.parent = this
        collision.active = collidable
    }

    override fun render(vp: Mat4, tickDelta: Double) {

        this.shape.bind()
        shader.use()

        shader.setMat4("model", transform.model)
        shader.setMat4("vp", vp)

        glActiveTexture(GL_TEXTURE0)
        texture.bind()

        drawArrays(this.shape.size)
    }

    override fun toData(): Array<String> {
        var string ="GameItem: \n" +
                    "\tShape: $shape\n"

        if(shader != defaultShader) string += "\tShader: $shader\n"
        if(texture != Texture.brokeTexture) string += "\tTexture: $texture\n"
        if(position != Vec2()) string += "\tPosition: $position\n"

        return arrayOf(string)
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