package com.pineypiney.game_engine.util.text

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.textures.Texture2D
import glm_.vec2.Vec2

class TextData(val mesh: Mesh?, val texture: Texture2D = Texture2D.missing, val deleteTexture: Boolean = false) : Deletable {

	constructor(factory: ResourceFactory, text: String, chars: Array<CharacterQuad>, texture: Texture2D = Texture2D.missing, deleteTexture: Boolean = false) :
			this(
				factory.createIndexedMesh(
					"Text '$text'",
					chars.flatMap { it.getVertices() }.toFloatArray(),
					createIndices(chars.size),
					Mesh.createAttributes(
						listOf(
							VertexAttribute.POSITION2D,
							VertexAttribute.TEX_COORD
						)
					)
				), texture, deleteTexture
			)

	override fun delete() {
		mesh?.delete()
		if (deleteTexture) texture.delete()
	}

	companion object {
		fun createIndices(numChars: Int): IntArray {
			return List(numChars) { c ->
				val i = c * 4
				listOf(i, i + 1, i + 2, i + 2, i + 3, i)
			}.flatten().toIntArray()
		}
	}

	class CharacterQuad(val bl: Vec2, val tr: Vec2, val tbl: Vec2, val ttr: Vec2) {

		fun getVertices(): List<Float> {
			return listOf(
				bl.x, bl.y, tbl.x, tbl.y,
				tr.x, bl.y, ttr.x, tbl.y,
				tr.x, tr.y, ttr.x, ttr.y,
				bl.x, tr.y, tbl.x, ttr.y,
			)
		}
	}
}