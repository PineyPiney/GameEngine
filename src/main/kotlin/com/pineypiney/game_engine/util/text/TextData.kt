package com.pineypiney.game_engine.util.text

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.util.DeletionQueue
import glm_.vec2.Vec2
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class TextData : Deletable {

	val mesh: Mesh?
	val texture: Texture2D
	val deletionQueue = DeletionQueue()
	val xOffsets: FloatArray
	val yOffset: Float

	constructor(){
		mesh = null
		texture = Texture2D.missing
		xOffsets = FloatArray(0)
		yOffset = 0f
	}

	constructor(factory: ResourceFactory, text: String, chars: Array<CharacterQuad>, texture: Texture2D = Texture2D.missing, xOffsets: FloatArray = FloatArray(0), yOffset: Float = 0f) {
		val positions = BufferUtils.createByteBuffer(chars.size * 32)
		val uvs = BufferUtils.createByteBuffer(chars.size * 32)
		for (char in chars) {
			char.getPositions(positions)
			char.getUVs(uvs)
		}
		val buffers: List<Pair<ByteBuffer, Map<VertexAttribute<*, *>, Long>>> = listOf(
			positions.flip() to mapOf(VertexAttribute.POSITION2D to 0L),
			uvs.flip() to mapOf(VertexAttribute.TEX_COORD to 0L)
		)
		mesh = factory.createIndexedMesh(
			"Text '$text'", buffers,
			createIndices(chars.size)
		)
		this.texture = texture
		this.xOffsets = xOffsets
		this.yOffset = yOffset
		deletionQueue.pushAll(mesh)
	}

	override fun delete() {
		deletionQueue.flush()
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

		fun getPositions(buffer: ByteBuffer): ByteBuffer = buffer
			.putFloat(bl.x).putFloat(bl.y)
			.putFloat(tr.x).putFloat(bl.y)
			.putFloat(tr.x).putFloat(tr.y)
			.putFloat(bl.x).putFloat(tr.y)

		fun getUVs(buffer: ByteBuffer): ByteBuffer = buffer
			.putFloat(tbl.x).putFloat(tbl.y)
			.putFloat(ttr.x).putFloat(tbl.y)
			.putFloat(ttr.x).putFloat(ttr.y)
			.putFloat(tbl.x).putFloat(ttr.y)

	}
}