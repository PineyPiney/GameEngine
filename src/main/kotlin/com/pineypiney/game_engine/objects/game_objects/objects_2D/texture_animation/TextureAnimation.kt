package com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation

import com.pineypiney.game_engine.resources.textures.Texture
import glm_.i

class TextureAnimation(val textures: Array<Texture>, val order: IntArray = IntArray(textures.size){it}, val length: Float = 1f): FrameSelector {

    constructor(textures: Array<Texture>, length: Float): this(textures, IntArray(textures.size){it}, length = length)

    // Throw errors if any of the constructor parameters are illegal
    init {
        if(textures.isEmpty()) throw IllegalArgumentException("Created Animation that has no textures, animations should have at least one texture")
        if(order.isEmpty()) throw IllegalArgumentException("Created Animation that uses none of the textures provided, order should contain at least 1 integer")
        order.firstOrNull { it >= textures.size }.let { if(it != null) throw IllegalArgumentException("Order contains int $it, which is too large for ${textures.size} textures") }
        if(length <= 0) throw IllegalArgumentException("Created Animation with length $length. Animation lengths should be greater than 0")
    }

    override fun getTexture(time: Float): Texture{
        val index = ((time / length) * order.size).i % order.size
        return textures[order[index]]
    }
}