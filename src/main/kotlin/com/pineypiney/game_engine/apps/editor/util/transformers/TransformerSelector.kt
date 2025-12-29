package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.menu_items.TextButton
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class TransformerSelector(screen: EditorScreen) : GameObject("Transformer Selector", 1){

	val moveButton = TextButton("M", Vec2(0f), Vec2(1f, 1f)){
		screen.setTransformer(Transformers.TRANSLATE2D)
	}
	val rotateButton = TextButton("R", Vec2(0f, -1f), Vec2(1f, 1f)){
		screen.setTransformer(Transformers.ROTATE2D)
	}
	val scaleButton = TextButton("S", Vec2(0f, -2f), Vec2(1f, 1f)){
		screen.setTransformer(Transformers.SCALE2D)
	}

	override fun init() {
		super.init()
		pixel(Vec2i(288, 0), Vec2i(20), Vec2(-1f, .4f))
	}

	override fun addChildren() {
		addChild(moveButton, rotateButton, scaleButton)
	}
}