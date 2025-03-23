package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PixelTransformComponent

abstract class Transformer(parent: GameObject, val screen: EditorScreen) : DefaultInteractorComponent(parent){

	val transform get() = parent.transformComponent as? PixelTransformComponent

	open fun startAt(obj: GameObject, screen: EditorScreen){
		transform?.origin = screen.renderer.camera.worldToScreen(obj.transformComponent.worldPosition)
	}
}