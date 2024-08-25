package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.window.WindowI

interface WindowRendererI<E : GameLogicI> : GameRendererI<E> {

	val window: WindowI

	val camera: CameraI
	fun updateAspectRatio(window: WindowI, objects: ObjectCollection)
}