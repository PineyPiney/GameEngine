package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2

class TextQuad(vecs: Array<Vec2>, val texture: Texture, val offset: Vec2) :
	SquareShape(vecs[0], vecs[1], vecs[2], vecs[3]) {

	override fun bind() {
		super.bind()
		texture.bind()
	}

	companion object {

	}
}
