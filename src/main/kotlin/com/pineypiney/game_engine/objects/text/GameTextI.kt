package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.util.Transform

interface GameTextI: TextI, Renderable {

    val transform: Transform

    override fun pixelToRelative(pixel: Int): Float {
        return pixel * (defaultCharWidth / font.letterWidth)
    }
}