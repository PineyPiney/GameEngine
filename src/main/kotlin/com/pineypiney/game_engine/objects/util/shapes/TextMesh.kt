package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2

class TextMesh(chars: Array<CharacterMesh>, val texture: Texture = Texture.broke, val deleteTexture: Boolean = false) : IndicesMesh(chars.flatMap { it.getVertices() }.toFloatArray(), intArrayOf(2, 2), createIndices(chars.size)) {

	override val shape: Shape<*> = Rect2D(Vec2(0f), 1f, 1f)

	init {
		positionSize = 2
		textureSize = 2
		textureOffset = 2
	}

	override fun delete() {
		super.delete()
		if(deleteTexture) texture.delete()
	}

	companion object {
		fun createIndices(numChars: Int): IntArray{
			return List(numChars){ c ->
				val i = c * 4
				listOf(i, i+1, i+2, i+2, i+3, i)
			}.flatten().toIntArray()
		}
	}

	class CharacterMesh(val bl: Vec2, val tr: Vec2, val tbl: Vec2, val ttr: Vec2){

		fun getVertices(): List<Float>{
			return listOf(
				bl.x, bl.y, tbl.x, tbl.y,
				bl.x, tr.y, tbl.x, ttr.y,
				tr.x, tr.y, ttr.x, ttr.y,
				tr.x, bl.y, ttr.x, tbl.y
			)
		}
	}
}