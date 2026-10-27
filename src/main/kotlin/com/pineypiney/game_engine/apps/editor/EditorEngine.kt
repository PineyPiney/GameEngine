package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.resources.DirectorySource
import com.pineypiney.game_engine.resources.GithubSource
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.Source
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.window.WindowedGameEngine

class EditorEngine(
	override val window: Window,
	manager: ApiManager,
	resources: ResourcesLoader,
	ups: Int = 20,
	fps: Int = 2000,
	val editorScreen: (EditorEngine, (WindowRendererI<EditorScreen>) -> PresentingApi) -> EditorScreen = ::EditorScreen
) : WindowedGameEngine<EditorScreen>(manager, resources) {

	constructor(
		window: Window,
		manager: ApiManager,
		sources: List<Source> = listOf(DirectorySource(), GithubSource.ENGINE),
		ups: Int = 20,
		fps: Int = 2000,
		editorScreen: (EditorEngine, (WindowRendererI<EditorScreen>) -> PresentingApi) -> EditorScreen = ::EditorScreen
	) : this(window, manager, ResourcesLoader(manager.createResourceFactory(), sources), ups, fps, editorScreen)

	override val TARGET_FPS: Int = fps
	override val TARGET_UPS: Int = ups

	override lateinit var activeScreen: EditorScreen

	override fun loadResources() {
		super.loadResources()
		GameEngineI.defaultFont = "Simplified Hans Light"
	}

	override fun setLogic() {
		activeScreen = editorScreen(this) { manager.createPresentingApi(window, "Editor") }
		deletionQueue.push(activeScreen)
	}
}