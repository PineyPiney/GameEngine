package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.audio.AudioEngine
import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.window.Window
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW
import org.lwjgl.openal.AL10
import java.io.File

class TestWindow(width: Int = 960, height: Int = 540, version: Vec2i): Window("Example Window", width, height, false, false, defaultHints + mapOf(
	GLFW.GLFW_CONTEXT_VERSION_MAJOR to version.x,
	GLFW.GLFW_CONTEXT_VERSION_MINOR to version.y,
)) {

	// input must be set after the windowHandle has been set so that the callbacks are assigned correctly
	override val input = DefaultInput(this)

	init {
		setIcon(File("src/main/resources/textures/menu_items/slider/pointer.png").inputStream())

		center()
	}

	override fun configureAL() {
		super.configureAL()
		setAudioInput(AudioEngine.getAllInputDevices().firstOrNull(), 44100, AL10.AL_FORMAT_MONO8, 4096)
	}
}