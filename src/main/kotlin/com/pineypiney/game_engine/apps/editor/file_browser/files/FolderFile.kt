package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import java.io.File

class FolderFile(parent: GameObject, file: File, browser: FileBrowser): FileComponent(parent, file, browser) {

	override fun open() {
		browser.currentDirectory = file
		browser.refreshDirectory()
	}

	override fun getIcon(center: Vec2, width: Int, height: Int): Sprite {
		val numChildren = file.listFiles()?.size ?: 0
		val start: Float = if(numChildren == 0) 0.6666667f
		else if(numChildren <= 3) 0.33333334f
		else 0f

		return Sprite(TextureLoader[ResourceKey("menu_items/folders")], 138f, center, Vec2(start, 0f), Vec2(0.33333334f, 1f))
	}
}