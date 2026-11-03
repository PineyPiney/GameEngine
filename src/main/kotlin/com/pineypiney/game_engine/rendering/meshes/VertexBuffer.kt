package com.pineypiney.game_engine.rendering.meshes

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.getOrNull
import java.nio.ByteBuffer

abstract class VertexBuffer : Deletable {

	abstract val stride: Int
	abstract val attributes: Map<VertexAttribute<*, *>, Long>

	fun hasAttribute(attribute: VertexAttribute<*, *>) = attributes.containsKey(attribute)

	abstract fun getData(): ByteBuffer

	fun <A> getAttribute(attribute: VertexAttribute<A, *>): List<A> {
		val buffer = getData()
		val list = mutableListOf<A>()
		val step = attributes.getOrNull(attribute)?.toInt() ?: return emptyList()
		for (i in 0..<buffer.limit() / stride) {
			list.add(attribute.get(buffer, i * stride + step))
		}
		return list
	}
}