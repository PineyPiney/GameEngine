package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.resources.ResourceFactory

interface ApiManager : Deletable {

	fun createResourceFactory(): ResourceFactory
	fun createPresentingApi(renderer: WindowRendererI<*>, name: String): PresentingApi
	fun waitIdle()
}