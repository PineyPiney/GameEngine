package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import java.io.File
import kotlin.math.max

class ImageFile(parent: GameObject, file: File, browser: FileBrowser): FileComponent(parent, file, browser) {

	override fun getIcon(center: Vec2, width: Int, height: Int): Sprite {
		val id = file.path.substring(28, file.path.length - 4)
		val tex = TextureLoader[ResourceKey(id)]
		return Sprite(tex, max(tex.width, tex.height).toFloat(), center)
	}
}