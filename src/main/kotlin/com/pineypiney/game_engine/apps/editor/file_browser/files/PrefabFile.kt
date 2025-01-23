package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.editor.file_browser.FileBrowser
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI
import com.pineypiney.game_engine.objects.prefabs.Prefab
import com.pineypiney.game_engine.rendering.ObjectRenderer
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.shapes.Shape3D
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xy
import glm_.vec4.Vec4
import java.io.File
import kotlin.math.max
import kotlin.math.min

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
		val prefab = Prefab(file)
		parent.addChild(prefab)
		prefab.init()
	}

	override fun getIcon(center: Vec2, width: Int, height: Int): Sprite {
		val prefab = Prefab(file).apply { init() }
		val renderers = prefab.allActiveDescendants().mapNotNull { it.getComponent<RenderedComponentI>() }
		if(renderers.isEmpty()) return super.getIcon(center, width, height)
		
		val minPos = Vec3(Float.MAX_VALUE)
		val maxPos = Vec3(-Float.MAX_VALUE)
		for(rendComp in renderers){
			val shape = rendComp.shape transformedBy rendComp.parent.worldModel
			minPos.xy = Vec2(min(minPos.x, shape.min.x), min(minPos.y, shape.min.y))
			maxPos.xy = Vec2(max(maxPos.x, shape.max.x), max(maxPos.y, shape.max.y))
			if(shape is Shape3D){
				minPos.z = min(minPos.z, shape.min.z)
				maxPos.z = max(maxPos.z, shape.max.z)
			}
			else {
				minPos.z = min(minPos.z, rendComp.parent.transformComponent.worldPosition.z)
				maxPos.z = min(minPos.z, rendComp.parent.transformComponent.worldPosition.z)
			}
		}
		val size = maxPos - minPos
		val scale = 2f / maxOf(size.x, size.y, size.z)
		prefab resize Vec3(scale)

		val offset = Vec3((minPos.x+maxPos.x) * .5f * scale, (minPos.y+maxPos.y) * .5f * scale, maxPos.z + 1f)
		prefab translate -offset

		val renderer = ObjectRenderer(Vec3(0f))
		renderer.init()
		GLFunc.clearColour = Vec4(0f)
		renderer.render(prefab)

		val texture = Texture(file.path, renderer.frameBuffer.TCB)
		return Sprite(texture, 64f)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 0 && Timer.time - fileSelect > .5){
			val obj = Prefab(file)
			obj.init()
			obj.position = Vec3(Vec2(browser.screen.renderer.camera.screenToWorld(Vec2(cursorPos.x / browser.screen.window.aspectRatio, cursorPos.y))), obj.position.z)
			browser.screen.objectBrowser.let {
				it.addRootObject(obj)
				it.positionNodes()
			}
			return INTERRUPT
		}
		return action
	}
}