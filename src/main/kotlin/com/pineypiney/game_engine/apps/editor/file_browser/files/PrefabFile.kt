package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import java.io.File

class PrefabFile(parent: GameObject, file: File, browser: FileBrowser): FileComponent(parent, file, browser) {

	override fun open() {
		browser.screen.sceneObjects.delete()
		browser.screen.loadPrefab(file)
	}

	override fun position(obj: GameObject, cursorPos: Vec2) {
		val newWorldPos = Vec3(Vec2(browser.screen.renderer.camera.screenToWorld(Vec2(cursorPos.x / browser.screen.window.aspectRatio, cursorPos.y))), obj.position.z)
		obj.transformComponent.worldPosition = newWorldPos
	}

	override fun addRenderer(parent: GameObject) {
		val prefab = GameObjectSerializer.parse(file.inputStream())
		parent.addChild(prefab)
		prefab.init()
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 0 && Timer.time - fileSelect > .5){
			val obj = GameObjectSerializer.parse(file.inputStream())
			obj.position = Vec3(Vec2(browser.screen.renderer.camera.screenToWorld(Vec2(cursorPos.x / browser.screen.window.aspectRatio, cursorPos.y))), obj.position.z)
			obj.init()
			browser.screen.sceneObjects.addObject(obj)
			browser.screen.objectBrowser.addRootObject(obj)
			return INTERRUPT
		}
		return action
	}
}