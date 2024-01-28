package com.pineypiney.game_engine.level_editor

import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import java.io.File

class PixelWindow(title: String, width: Int, height: Int, vSync: Boolean) : Window(title, width, height, false, vSync) {

    override val input: Inputs = DefaultInput(this)

    init {
        setIcon(File("src/main/resources/textures/menu_items/slider/pointer.png").inputStream())
    }

    companion object{
        val INSTANCE = PixelWindow("Pixel Game Editor", 960, 540, false)
    }
}