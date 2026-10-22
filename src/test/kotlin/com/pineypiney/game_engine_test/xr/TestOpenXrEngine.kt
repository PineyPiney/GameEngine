package com.pineypiney.game_engine_test.xr

import com.pineypiney.game_engine.resources.DirectorySource
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.xr.OpenXrGameEngine
import com.pineypiney.game_engine.xr.XrGraphicsApi

class TestOpenXrEngine(window: WindowI, api: XrGraphicsApi, val screen: (TestOpenXrEngine) -> OpenXrTest = ::OpenXrTest): OpenXrGameEngine<OpenXrTest>(window, api) {

	override val actionSet: TestActions = TestActions()
	override lateinit var activeScreen: OpenXrTest private set

	override fun createResourcesLoader(factory: ResourceFactory): ResourcesLoader = ResourcesLoader(factory, listOf(DirectorySource("src/main/resources"), DirectorySource("src/test/resources")))

	override fun setLogic() {
		activeScreen = screen(this)
		deletionQueue.push(activeScreen)
	}
}