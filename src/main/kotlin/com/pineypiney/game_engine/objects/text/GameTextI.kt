package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform2D

interface GameTextI: TextI, Renderable {

    val transform: Transform2D

    override fun pixelToRelative(pixel: Int): Float {
        return pixel * (defaultCharWidth / font.letterWidth)
    }
}