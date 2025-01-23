package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import glm_.vec3.Vec3

abstract class Transformer(parent: GameObject, val screen: EditorScreen) : DefaultInteractorComponent(parent){

	open fun startAt(obj: GameObject, screen: EditorScreen){
		parent.position = screen.renderer.camera.worldToScreen(obj.transformComponent.worldPosition).let{ Vec3(it.x * screen.window.aspectRatio, it.y, 0f) }
	}
}