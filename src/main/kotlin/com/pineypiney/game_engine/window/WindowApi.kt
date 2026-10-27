package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.objects.Initialisable

interface WindowApi : Initialisable {

	fun createWindow(title: String, width: Int, height: Int, properties: WindowProperties): Window

	fun getVulkanExtensions(): List<String>
}