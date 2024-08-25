package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.Components
import com.pineypiney.game_engine.util.extension_functions.toByteString
import glm_.getInt
import glm_.int
import java.io.InputStream
import java.nio.charset.Charset

class GameObjectSerializer {

	companion object {
		fun serialise(obj: GameObject): String {
			val h = StringBuilder()
			val d = StringBuilder()
			serialiseParts(obj, h, d)
			return h.length.toByteString() + h.toString() + d.toString()
		}

		private fun serialiseParts(
			obj: GameObject,
			head: StringBuilder = StringBuilder(),
			data: StringBuilder = StringBuilder()
		) {
			head.append(obj.name.length.toByteString(2) + obj.name)
			head.append("act" + if (obj.active) 1.toChar() else 0.toChar())
			head.append("lay${obj.layer.toByteString(4)}")

			val parts = mutableListOf<Pair<StringBuilder, StringBuilder>>()
			if (obj.components.isNotEmpty()) parts.add(addListPart("COMP", obj.components, ComponentI::serialise))
			if (obj.children.isNotEmpty()) parts.add(addListPart("CHLD", obj.children, ::serialiseParts))

			head.append(parts.size.toByteString())
			for ((h, d) in parts) {
				head.append(h)
				data.append(d)
			}
		}

		private fun <T> addListPart(
			name: String,
			list: Collection<T>,
			transform: (T, StringBuilder, StringBuilder) -> Unit
		): Pair<StringBuilder, StringBuilder> {
			val head = StringBuilder(name + list.size.toByteString())
			val data = StringBuilder()
			list.forEach { transform(it, head, data) }
			return head to data
		}

		fun parse(stream: InputStream): GameObject {
			try {
				val headLen = stream.int()
				val head = stream.readNBytes(headLen).inputStream()
				return parseChild(head, stream)
			} catch (e: Exception) {
				return GameObject("Womp Womp")
			}
		}

		fun parseChild(head: InputStream, data: InputStream): GameObject {
			val nameLen =
				head.readNBytes(2).reversed().withIndex().sumOf { (index, byte) -> (byte.toInt() shl (index * 8)) }
			val name = head.readNBytes(nameLen).toString(Charset.defaultCharset())
			val o = GameObject(name)

			val active = head.readNBytes(4)
			val layer = head.readNBytes(7)

			o.active = active[3] > 0
			o.layer = layer.getInt(3)

			val partCount = head.int()

			for (i in 1..partCount) {
				val type = head.readNBytes(4).toString(Charset.defaultCharset())
				when (type) {
					"COMP" -> parseComponents(head, data, o)
					"CHLD" -> {
						var childrenSize = head.int()
						while (childrenSize-- > 0) {
							o.addChild(parseChild(head, data))
						}
					}
				}
			}


			return o
		}

		fun parseComponents(head: InputStream, data: InputStream, parent: GameObject) {
			val componentCount = head.int()
			for (cn in 1..componentCount) {
				val nameSize = head.read()
				val componentName = head.readNBytes(nameSize).toString(Charset.defaultCharset())
				val component = Components.createComponent(componentName, parent)
				val numFields = head.read()

				if (component == null) {
					head.skipNBytes(numFields * 7L)
					continue
				}

				for (fn in 1..numFields) {
					val fieldName = head.readNBytes(3).toString(Charset.defaultCharset())
					val fieldSize = head.int()
					val value = data.readNBytes(fieldSize).toString(Charsets.ISO_8859_1)
					component.setValue(fieldName, value)
				}
				parent.components.add(component)
			}
		}
	}
}