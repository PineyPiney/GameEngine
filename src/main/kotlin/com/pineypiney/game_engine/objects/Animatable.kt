package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.resources.textures.Texture

interface Animatable {

    var movingFrames: List<Texture>
    var stillFrames: List<Texture>
    var currentFrame: Texture
    val animationLength: Float
    var animationOffset: Float

    fun calcCurrentFrame(): Texture
}