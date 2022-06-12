package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.mat4x4.Mat4

open class TexturedGameObject(override val id: ResourceKey, var texture: Texture, val shape: Shape = Shape.centerSquareShape3D, shader: Shader = defaultShader) : RenderedGameObject(), Shaded {

    constructor(id: ResourceKey, textureKey: ResourceKey, shape: Shape = Shape.centerSquareShape3D, shader: Shader = defaultShader):
            this(id, TextureLoader.getTexture(textureKey), shape, shader)

    override var shader: Shader = shader
        set(value){
            field = value
            uniforms = field.compileUniforms()
        }
    override var uniforms: Uniforms = Uniforms.default
        set(value) {
            field = value
            setUniforms()
        }

    override fun init() {
        uniforms = shader.compileUniforms()
    }

    override fun setUniforms() {
        uniforms.setMat4Uniform("model"){ transform.model }
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {

        shader.use()
        shader.setUniforms(uniforms)
        shader.setMat4("view", view)
        shader.setMat4("projection", projection)

        texture.bind()

        shape.bind()
        shape.draw()
    }

    override fun copy(): TexturedGameObject {
        return this.clone() as TexturedGameObject
    }
}