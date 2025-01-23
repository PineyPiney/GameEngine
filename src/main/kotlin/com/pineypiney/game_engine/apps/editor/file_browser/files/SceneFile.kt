package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.objects.GameObject
import java.io.File

class SceneFile(parent: GameObject, file: File, browser: FileBrowser): FileComponent(parent, file, browser) {

	override fun open() {
		browser.screen.sceneObjects.delete()
		browser.screen.loadScene(file)
	}
}