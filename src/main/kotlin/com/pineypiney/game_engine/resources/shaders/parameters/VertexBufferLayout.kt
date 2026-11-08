package com.pineypiney.game_engine.resources.shaders.parameters

import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.serialisation.Codec

data class VertexBufferLayout(val layout: List<Binding>) {

	constructor(bindings: Map<Int, Pair<Int, Collection<VertexAttribute<*, *>>>>) : this(bindings.map { (i, binding) ->
		Binding.consecutive(i, binding.first, binding.second)
	})

	data class Attribute(val location: Int, val format: TextureFormat, val offset: Int) {
		constructor(location: Int, attrib: VertexAttribute<*, *>, offset: Int) : this(location, attrib.format, offset)
	}

	data class Binding(val location: Int, val attributes: List<Attribute>, val stride: Int) {
		companion object {
			fun consecutive(location: Int, firstIndex: Int, attributes: Collection<VertexAttribute<*, *>>): Binding {
				val attribs = mutableListOf<Attribute>()
				var index = firstIndex
				var offset = 0
				for (att in attributes) {
					attribs.add(Attribute(index++, att.format, offset))
					offset += att.bytes
				}
				return Binding(location, attribs, offset)
			}
		}
	}

	companion object {
		val ATTRIB_CODEC = Codec.map(
			Codec.INT.field("location", Attribute::location),
			Codec.enum(TextureFormat::valueOf).field("format", Attribute::format),
			Codec.INT.field("offset", Attribute::offset),
			::Attribute
		)
		val BINDING_CODEC = Codec.map(
			Codec.INT.field("location", Binding::location),
			ATTRIB_CODEC.list().field("attribs", Binding::attributes),
			Codec.INT.field("stride", Binding::stride),
			::Binding
		)
		val CODEC = BINDING_CODEC.list().optional(emptyList()).map(VertexBufferLayout::layout, ::VertexBufferLayout)

		fun consecutive(vararg attributes: Collection<VertexAttribute<*, *>>): VertexBufferLayout {
			val layout = mutableListOf<Binding>()
			var index = 0
			for ((i, binding) in attributes.withIndex()) {
				val binding = Binding.consecutive(i, index, binding)
				layout.add(binding)
				index += binding.attributes.size
			}
			return VertexBufferLayout(layout)
		}

		fun consecutiveFormats(vararg formats: Collection<TextureFormat>): VertexBufferLayout {
			val layout = mutableListOf<Binding>()
			var index = 0
			for ((i, binding) in formats.withIndex()) {
				val attributes = mutableListOf<Attribute>()
				var offset = 0
				for (format in binding) {
					attributes.add(Attribute(index++, format, offset))
					offset += format.pixelSize
				}
				layout.add(Binding(i, attributes, offset))
			}
			return VertexBufferLayout(layout)
		}

		val POS2D = consecutive(
			listOf(VertexAttribute.POSITION2D)
		)

		val POS_TEX = consecutiveFormats(
			listOf(TextureFormat.RG32F),
			listOf(TextureFormat.RGB32F)
		)

		val POS_TEXNOR = consecutive(
			listOf(VertexAttribute.POSITION),
			listOf(VertexAttribute.TEX_COORD, VertexAttribute.NORMAL)
		)

		val POS_TEXNOR_BONES = consecutive(
			listOf(VertexAttribute.POSITION),
			listOf(VertexAttribute.TEX_COORD, VertexAttribute.NORMAL),
			listOf(VertexAttribute.BONE_IDS, VertexAttribute.BONE_WEIGHTS)
		)

		val POS_TEXNOR_TAN = consecutive(
			listOf(VertexAttribute.POSITION),
			listOf(VertexAttribute.TEX_COORD, VertexAttribute.NORMAL),
			listOf(VertexAttribute.TANGENT),
		)
	}
}