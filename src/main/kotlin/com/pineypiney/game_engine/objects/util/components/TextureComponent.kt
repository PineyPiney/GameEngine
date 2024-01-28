package com.pineypiney.game_engine.objects.util.components

import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey

open class TextureComponent(parent: Storable, var texture: Texture): Component("TXR", parent) {

    constructor(parent: Storable): this(parent, Texture.broke)

    override val fields: Array<Field<*>> = arrayOf(
        Field("txr", ::DefaultFieldEditor, ::texture, { texture = it }, { it.fileLocation.substringBefore('.') }, { _, s -> TextureLoader[ResourceKey(s)]} )
    )
}