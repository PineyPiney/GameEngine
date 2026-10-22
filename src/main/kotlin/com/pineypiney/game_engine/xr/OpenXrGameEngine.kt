package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.xr.actions.OpenXrActionSet
import org.lwjgl.glfw.GLFW

abstract class OpenXrGameEngine<E: OpenXrGameLogicI>(val window: WindowI, val xrGraphicsApi: XrGraphicsApi) : GameEngine<E>() {

	lateinit var openxr: OpenXrManager
	final override lateinit var resourcesLoader: ResourcesLoader private set

	abstract val actionSet: OpenXrActionSet

	override fun init() {
		openxr = OpenXrManager(window, xrGraphicsApi, actionSet)
		resourcesLoader = createResourcesLoader(openxr.manager.createResourceFactory())
		super.init()
		activeScreen.open()
	}

	open fun createResourcesLoader(factory: ResourceFactory) = ResourcesLoader.development(factory)

	override fun loadResources() {
		super.loadResources()
		GameEngineI.defaultFont = "Simplified Hans Light"
	}

	override fun update(interval: Float) {
		super.update(interval)
		activeScreen.update(interval, window.input)
	}

	override fun render(tickDelta: Double) {
		if(openxr.session.running) openxr.renderManager.render(activeScreen, tickDelta)
		window.update()
	}

	override fun input() {
		if(!openxr.session.pollEvents()) window.shouldClose = true
	}

	override fun shouldRun(): Boolean = !window.shouldClose

	override fun cleanUp() {
		openxr.manager.waitIdle()
		super.cleanUp()
		GLFW.glfwTerminate()
		openxr.delete()
	}
}