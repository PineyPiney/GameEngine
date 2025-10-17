package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.apps.editor.util.EditorPositioningComponent
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.prefabs.Prefab
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import java.io.File
import kotlin.math.max
import kotlin.math.min

class PrefabFile(parent: GameObject, file: File, browser: FileBrowser): FileComponent(parent, file, browser) {

	override fun position(obj: GameObject, cursorPos: Vec2, scenePosition: CursorPosition) {
		if(scenePosition.pixels.x == -1) super.position(obj, cursorPos, scenePosition)
		else obj.position = Vec3(scenePosition.position + Vec2(browser.screen.renderer.camera.cameraPos), 0f)
	}

	override fun addRenderer(parent: GameObject, cursor: CursorPosition) {
		super.addRenderer(parent, cursor)
		val sceneRenderer = MenuItem("Scene Renderer")

		val prefab = Prefab(file)
		prefab.parseAndEdit()

		sceneRenderer.addChild(prefab)
		parent.addChild(sceneRenderer)
	}

	override fun getIcon(center: Vec2, width: Int, height: Int): Sprite {
		val cachedTexture = browser.loadedTextures[file.path]
		if(cachedTexture != null){
			return Sprite(cachedTexture, 64f, center)
		}
		val prefab = Prefab(file).apply { parseAndEdit(); init(); position = Vec3(0f) }
		val renderers = prefab.allActiveDescendants().mapNotNull { it.getComponent<RenderedComponentI>() }
		if(renderers.isEmpty()) return super.getIcon(center, width, height)
		
		val minPos = Vec3(Float.MAX_VALUE)
		val maxPos = Vec3(-Float.MAX_VALUE)
		for(rendComp in renderers){
			for(mesh in rendComp.getMeshes()) {
				val (meshMin, meshMax) = mesh.getBounds(rendComp.parent.worldModel)
				minPos(min(minPos.x, meshMin.x), min(minPos.y, meshMin.y), min(minPos.z, meshMin.z))
				maxPos(max(maxPos.x, meshMax.x), max(maxPos.y, meshMax.y), max(maxPos.z, meshMax.z))
			}
		}
		val size = maxPos - minPos
		val scale = 2f / maxOf(size.x, size.y, size.z)
		prefab resize Vec3(scale)

		val offset = Vec3((minPos.x+maxPos.x) * .5f * scale, (minPos.y+maxPos.y) * .5f * scale, maxPos.z + 1f)
		prefab translate -offset

		GLFunc.clearColour = Vec4(0f)
		browser.prefabRenderer.render(prefab)
		val texture = browser.prefabRenderer.getTexture(prefab.name)
		browser.loadedTextures[file.path] = texture
		return Sprite(texture, 64f, center)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 0 && Timer.time - fileSelect > .5){
			val obj = Prefab(file)
			obj.parseAndEdit()
			obj.init()

			val placingComponent = obj.getComponent<EditorPositioningComponent>()
			val scenePosition = browser.screen.getSceneCursorPosition(cursorPos)
			var newWorldPos = Vec3(scenePosition.position + Vec2(browser.screen.renderer.camera.cameraPos), 0f)
			if(placingComponent != null) newWorldPos = placingComponent.place(obj.transformComponent.worldPosition, newWorldPos)
			obj.position = newWorldPos

			browser.screen.objectBrowser.let {
				it.addRootObject(obj)
				it.positionNodes()
			}
			return INTERRUPT
		}
		return action
	}
}