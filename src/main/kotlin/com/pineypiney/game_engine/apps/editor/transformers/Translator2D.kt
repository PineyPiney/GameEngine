package com.pineypiney.game_engine.apps.editor.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.abs

class 	Translator2D(parent: GameObject, screen: EditorScreen) : Transformer(parent, screen){

	val xArrow by lazy {
		parent.getChild("X Arrow")?.getComponent<ColouredSpriteComponent>()
	}
	val yArrow by lazy {
		parent.getChild("Y Arrow")?.getComponent<ColouredSpriteComponent>()
	}
	val box by lazy{
		parent.getChild("Box")?.getComponent<ColouredSpriteComponent>()
	}

	var selected = 0

	val red = Vec4(.8f, 0f, 0f, 1f)
	val green = Vec4(0f, .8f, 0f, 1f)
	val blue = Vec4(0f, 0f, .8f, 1f)

	override var forceUpdate: Boolean = true

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if(pressed) return

		val relX = cursorPos.x - parent.transformComponent.position.x
		val relY = cursorPos.y - parent.transformComponent.position.y
		if(abs(relY) < .012f && relX > 0f){
			red.x = 1f
			green.y = .8f
			blue.z = .8f
		}
		else if(abs(relX) < .012f && relY > 0f){
			red.x = .8f
			green.y = 1f
			blue.z = .8f
		}
		else if(relX > 0f && relX < .1f && relY > 0f && relY < .1f){
			red.x = .8f
			green.y = .8f
			blue.z = 1f
		}
		else {
			red.x = .8f
			green.y = .8f
			blue.z = .8f
		}
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		selected = when(1f){
			red.x -> 1
			green.y -> 2
			blue.z -> 3
			else -> 0
		}
		return action
	}

	override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		when(selected){
			0 -> return
			1 -> parent.translate(Vec3(cursorDelta.x, 0f, 0f))
			2 -> parent.translate(Vec3(0f, cursorDelta.y, 0f))
			3 -> parent.translate(Vec3(cursorDelta, 0f))
		}
		val newWorldPos = Vec3(Vec2(screen.renderer.camera.screenToWorld(Vec2(parent.position.x / window.aspectRatio, parent.position.y))), screen.editingObject!!.position.z)
		screen.editingObject?.transformComponent?.worldPosition = newWorldPos
		//screen.editingObject?.transformComponent?.worldModel?.let{ it.setTranslation(newWorldPos, it) }
		screen.componentBrowser.refreshField("T2D.pos")
	}
}