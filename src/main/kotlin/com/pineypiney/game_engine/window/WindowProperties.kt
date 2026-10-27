package com.pineypiney.game_engine.window

class WindowProperties() {

	val flags: MutableList<WindowFlags> = mutableListOf()
	var graphicsApi: Graphics = Graphics.OPENGL(3, 3)
	var visible: Boolean = true
	var decorated: Boolean = true
	var vSync: Boolean = false
	var fullscreen: Boolean = false

	fun addFlag(flag: WindowFlags): WindowProperties {
		flags.add(flag)
		return this
	}
	fun opengl(major: Int, minor: Int, multisamples: Int = 4): WindowProperties {
		graphicsApi = Graphics.OPENGL(major, minor, multisamples)
		return this
	}
	fun vulkan(): WindowProperties {
		graphicsApi = Graphics.VULKAN
		return this
	}
	fun visible(enabled: Boolean): WindowProperties {
		visible = enabled
		return this
	}
	fun decorated(enabled: Boolean): WindowProperties {
		decorated = enabled
		return this
	}
	fun vSync(enabled: Boolean): WindowProperties {
		vSync = enabled
		return this
	}
	fun fullscreen(enabled: Boolean): WindowProperties {
		fullscreen = enabled
		return this
	}

	sealed class Graphics {
		data class OPENGL(val major: Int, val minor: Int, val multisamples: Int = 4) : Graphics()
		data object VULKAN : Graphics()
	}
}