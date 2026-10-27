package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.window.Window

interface WindowRendererI<in E : GameLogicI> : GameRendererI<E> {

	val window: Window
	val camera: CameraI

	fun updateAspectRatio(window: Window, objects: ObjectCollection)

	override fun getRenderingApi(): PresentingApi
}