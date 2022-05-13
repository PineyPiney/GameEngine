package com.pineypiney.game_engine.visual

import com.pineypiney.game_engine.resources.textures.Texture

interface Animatable {

    var movingFrames: List<Texture>
    var stillFrames: List<Texture>
    var currentFrame: Texture
    val animationLength: Float
    var animationOffset: Float

    fun calcCurrentFrame(): Texture
}