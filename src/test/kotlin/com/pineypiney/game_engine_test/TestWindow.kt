package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.audio.AudioEngine
import com.pineypiney.game_engine.util.input.DefaultInput
import org.lwjgl.glfw.GLFW
import org.lwjgl.openal.AL10
import java.io.File

class TestWindow(width: Int = 960, height: Int = 540): Window("Example Window", width, height, false, true, hints) {

    // input must be set after the windowHandle has been set so that the callbacks are assigned correctly
    override val input = DefaultInput(this)

    init {
        setIcon(File("src/main/resources/textures/menu_items/slider/pointer.png").inputStream())

        autoIconify = true

        center()
    }

    override fun configureAl() {
        super.configureAl()
        setAudioInput(AudioEngine.getAllInputDevices().firstOrNull(), 44100, AL10.AL_FORMAT_MONO8, 4096)
    }

    companion object {
        val hints = defaultHints + mapOf(
            GLFW.GLFW_CONTEXT_VERSION_MAJOR to 3,
            GLFW.GLFW_CONTEXT_VERSION_MINOR to 3,
            GLFW.GLFW_SAMPLES to 4
        )

        val INSTANCE = TestWindow()
    }
}