package com.pineypiney.game_engine.util.extension_functions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec4.Vec4

fun JsonObject.getBoolean(key: String) = getAsJsonPrimitive(key).asBoolean
fun JsonObject.getByte(key: String) = getAsJsonPrimitive(key).asByte
fun JsonObject.getShort(key: String) = getAsJsonPrimitive(key).asShort
fun JsonObject.getInt(key: String) = getAsJsonPrimitive(key).asInt
fun JsonObject.getLong(key: String) = getAsJsonPrimitive(key).asLong
fun JsonObject.getFloat(key: String) = getAsJsonPrimitive(key).asFloat
fun JsonObject.getDouble(key: String) = getAsJsonPrimitive(key).asDouble
fun JsonObject.getString(key: String) = getAsJsonPrimitive(key).asString

fun JsonObject.getObjectOrNull(key: String): JsonObject? {
	return get(key) as? JsonObject
}

fun JsonObject.getArrayOrNull(key: String): JsonArray? {
	return get(key) as? JsonArray
}

fun JsonArray.getObject(i: Int): JsonObject {
	return get(i) as JsonObject
}

fun JsonArray.getObjectOrNull(i: Int): JsonObject? {
	return get(i) as? JsonObject
}

fun JsonArray.getArrayOrNull(i: Int): JsonArray? {
	return get(i) as? JsonArray
}

fun JsonObject.getIntOrNull(key: String): Int? = if (has(key)) getInt(key) else null
fun JsonObject.getFloatOrNull(key: String): Float? = if (has(key)) getFloat(key) else null
fun JsonObject.getStringOrNull(key: String): String? = if (has(key)) getString(key) else null
fun JsonArray.forEachObject(predicate: (JsonObject, Int) -> Unit) {
	for (i in 0..<size()) predicate(getObject(i), i)
}

fun <R> JsonArray.mapObjects(predicate: (JsonObject, Int) -> R): List<R> {
    val map = mutableListOf<R>()
	for (i in (0..<size())) map.add(predicate(getObject(i), i))
    return map.toList()
}

val JsonArray.objects get() = (0..<size()).associateWith { getObject(it) }

fun JsonObject.getVec3(name: String, offset: Int = 0): Vec3? {
	return if (has(name)) Vec3(getAsJsonArray(name), offset)
    else null
}

fun JsonObject.getVec4(name: String, offset: Int = 0): Vec4? {
	return if (has(name)) Vec4(getAsJsonArray(name), offset)
    else null
}

fun JsonObject.getQuat(name: String, offset: Int = 0): Quat? {
	return if (has(name)) Quat(Vec4(getAsJsonArray(name), offset))
    else null
}

fun JsonObject.with(key: String, property: Number?): JsonObject {
	addProperty(key, property)
	return this
}

fun JsonObject.with(key: String, property: String?): JsonObject {
	addProperty(key, property)
	return this
}

fun JsonObject.with(key: String, property: JsonElement?): JsonObject {
	add(key, property)
	return this
}

fun JsonArray.with(property: String?): JsonArray {
	add(property)
	return this
}

fun JsonArray.with(property: JsonElement?): JsonArray {
	add(property)
	return this
}

fun JsonArray.with(iterable: Iterable<Number?>): JsonArray {
	for (num in iterable) add(num)
	return this
}
