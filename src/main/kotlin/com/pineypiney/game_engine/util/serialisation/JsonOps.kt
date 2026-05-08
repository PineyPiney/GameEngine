package com.pineypiney.game_engine.util.serialisation

import com.google.gson.*
import com.pineypiney.game_engine.util.SuppliedIterator
import com.pineypiney.game_engine.util.extension_functions.with
import java.io.Reader


object JsonOps : SerialOps<JsonElement> {

	override fun nul(): JsonElement = JsonNull.INSTANCE
	override fun missing(): JsonElement = JsonMissing
	override fun parse(reader: Reader): JsonElement = JsonParser.parseReader(reader)

	override fun readBool(input: JsonElement?): Boolean = input?.asBoolean ?: false
	override fun readByte(input: JsonElement?): Byte = input?.asByte ?: 0
	override fun readShort(input: JsonElement?): Short = input?.asShort ?: 0
	override fun readInt(input: JsonElement?): Int = input?.asInt ?: 0
	override fun readLong(input: JsonElement?): Long = input?.asLong ?: 0
	override fun readFloat(input: JsonElement?): Float = input?.asFloat ?: 0f
	override fun readDouble(input: JsonElement?): Double = input?.asDouble ?: 0.0
	override fun readString(input: JsonElement?): String = input?.asString ?: ""

	override fun writeBool(input: Boolean): JsonElement = JsonPrimitive(input)
	override fun writeByte(input: Byte): JsonElement = JsonPrimitive(input)
	override fun writeShort(input: Short): JsonElement = JsonPrimitive(input)
	override fun writeInt(input: Int): JsonElement = JsonPrimitive(input)
	override fun writeLong(input: Long): JsonElement = JsonPrimitive(input)
	override fun writeFloat(input: Float): JsonElement = JsonPrimitive(input)
	override fun writeDouble(input: Double): JsonElement = JsonPrimitive(input)
	override fun writeNumber(input: Number): JsonElement = JsonPrimitive(input)
	override fun writeString(input: String): JsonElement = JsonPrimitive(input)

	override fun readInts(input: JsonElement?): Iterable<Int> {
		return if (input is JsonArray) input.map { it.asInt }
		else emptyList()
	}

	override fun readFloats(input: JsonElement?): Iterable<Float> {
		return if (input is JsonArray) input.map { it.asFloat }
		else emptyList()
	}

	override fun writeInts(input: Iterable<Int>): JsonElement {
		return JsonArray().with(input)
	}

	override fun writeFloats(input: Iterable<Float>): JsonElement {
		return JsonArray().with(input)
	}

	override fun createArray(): JsonElement = JsonArray()
	override fun createArray(first: JsonElement): JsonElement {
		return if (first == missing()) JsonArray()
		else JsonArray().with(first)
	}

	override fun createMap(): JsonElement = JsonObject()
	override fun createMap(key: String, first: JsonElement): JsonElement {
		return if (first == missing()) JsonObject()
		else JsonObject().with(key, first)
	}

	override fun appendArray(array: JsonElement, value: JsonElement) {
		if (array !is JsonArray) throw UnsupportedOperationException()
		else if (value != missing()) array.add(value)
	}

	override fun appendMap(map: JsonElement, key: String, value: JsonElement) {
		if (map !is JsonObject) throw UnsupportedOperationException()
		else if (value != missing()) map.add(key, value)
	}

	override fun hasChild(parent: JsonElement, name: String): Boolean {
		return parent is JsonObject && parent.has(name)
	}

	override fun getChild(parent: JsonElement, name: String): JsonElement {
		if (parent !is JsonObject) return nul()
		return parent.get(name) ?: missing()
	}


	override fun iterator(parent: JsonElement): Iterator<JsonElement> {
		return when (parent) {
			is JsonArray -> parent.iterator()
			is JsonObject -> SuppliedIterator(parent.entrySet(), Map.Entry<String, JsonElement>::value)
			else -> emptyList<JsonElement>().iterator()
		}
	}

	override fun mapIterator(parent: JsonElement): Iterator<Map.Entry<String, JsonElement>> {
		return if (parent is JsonObject) parent.entrySet().iterator()
		else emptyMap<String, JsonElement>().iterator()
	}

	override fun forEach(parent: JsonElement, action: (JsonElement) -> Unit) {
		when (parent) {
			is JsonArray -> parent.forEach { action(it) }
			is JsonObject -> parent.entrySet().forEach { action(it.value) }
		}
	}

	override fun forEachEntry(parent: JsonElement, action: (Map.Entry<String, JsonElement>) -> Unit) {
		if (parent is JsonObject) parent.entrySet().forEach(action)
	}

	override fun stringify(value: JsonElement): String = value.toString()
}