package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.util.input.DefaultInput
import glm_.vec2.Vec2i
import java.io.File

class ExampleWindow(title: String, width: Int, height: Int, vSync: Boolean): Window(title, width, height, vSync, Vec2i(3, 3)) {

    // input must be set after the windowHandle has been set so that the callbacks are assigned correctly
    override val input = DefaultInput(this)

    init {
        setIcon(File("src/main/resources/textures/menu_items/slider/pointer.png").inputStream())
    }
}