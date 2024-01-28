package com.pineypiney.game_engine.objects.util.components

import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec3.Vec3

open class ColouredTextureComponent(parent: Storable, texture: Texture, var colour: Vec3 = Vec3(1f, 1f, 1f)): TextureComponent(parent, texture) {

    constructor(parent: Storable): this(parent, Texture.broke, Vec3(1f, 1f, 1f))

    override val fields: Array<Field<*>> = super.fields + arrayOf(
        Vec3Field("clr", ::colour) { colour = it }
    )
}