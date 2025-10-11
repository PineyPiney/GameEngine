package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2

abstract class Transformer(parent: GameObject, val screen: EditorScreen) : DefaultInteractorComponent(parent){

	val transform get() = parent.transformComponent as? PixelTransformComponent

	open fun startAt(obj: GameObject, screen: EditorScreen){
		transform?.origin = screen.renderer.camera.worldToScreen(obj.transformComponent.worldPosition)
	}

	fun getCursorPos(cursor: CursorPosition) = Vec2(cursor.screenSpace.x * screen.renderer.camera.aspectRatio, cursor.screenSpace.y)

	override fun checkHover(ray: Ray, cursor: CursorPosition): Float {
		val pos = getCursorPos(cursor)
		return if(pos.isWithin(
				Vec2(parent.transformComponent.worldPosition),
				Vec2(parent.transformComponent.worldScale)
			)) ray.rayOrigin.z - parent.transformComponent.worldPosition.z
		else -1f
	}
}