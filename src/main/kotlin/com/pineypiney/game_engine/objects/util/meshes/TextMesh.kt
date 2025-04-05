package com.pineypiney.game_engine.objects.util.meshes

import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.maxOf
import com.pineypiney.game_engine.util.extension_functions.minOf
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2

class TextMesh(chars: Array<CharacterMesh>, val texture: Texture = Texture.broke, val deleteTexture: Boolean = false) : IndicesMesh(chars.flatMap { it.getVertices() }.toFloatArray(), arrayOf(
	VertexAttribute.POSITION2D, VertexAttribute.TEX_COORD
), createIndices(chars.size)) {

	override val shape: Rect2D = createShape(chars)

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

		fun createShape(chars: Array<CharacterMesh>): Rect2D{
			return if(chars.isEmpty()){
				Rect2D(0f, 0f, 0f, 0f)
			}
			else {
				var bl = chars[0].bl
				var tr = chars[0].tr
				for (i in 1..<chars.size) {
					bl = minOf(bl, chars[i].bl)
					tr = maxOf(tr, chars[i].tr)
				}
				Rect2D(bl, tr - bl)
			}
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