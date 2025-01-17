package com.pineypiney.game_engine.apps.editor.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.TextButton
import glm_.vec2.Vec2

class TransformerSelector(screen: EditorScreen) : MenuItem("Transformer Selector"){

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
		relative(Vec2(-.69f, .4f), Vec2(.1f))
	}

	override fun addChildren() {
		addChild(moveButton, rotateButton, scaleButton)
	}
}