package com.pineypiney.game_engine.util.extension_functions

import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.json.JSONArray
import org.json.JSONObject


fun JSONObject.getOrNull(key: String): Any?{
    return if(has(key)) get(key) else null
}

fun JSONObject.getIntOrNull(key: String): Int? = if(has(key)) getInt(key) else null

fun JSONObject.getFloatOrNull(key: String): Float? = if(has(key)) getFloat(key) else null

fun JSONObject.getStringOrNull(key: String): String? = if(has(key)) getString(key) else null

fun JSONArray.forEachObject(predicate: (JSONObject, Int) -> Unit) {
    for (i in 0..<length()) predicate(getJSONObject(i), i)
}

fun <R> JSONArray.mapObjects(predicate: (JSONObject, Int) -> R): List<R> {
    val map = mutableListOf<R>()
    for (i in (0..<length())) map.add(predicate(getJSONObject(i), i))
    return map.toList()
}

val JSONArray.objects get() = (0..<length()).associateWith { getJSONObject(it)!! }

fun JSONObject.getVec3(name: String, offset: Int = 0): Vec3?{
    return if(has(name)) Vec3(getJSONArray(name), offset)
    else null
}

fun JSONObject.getVec4(name: String, offset: Int = 0): Vec4?{
    return if(has(name)) Vec4(getJSONArray(name), offset)
    else null
}

fun JSONObject.getQuat(name: String, offset: Int = 0): Quat?{
    return if(has(name)) Quat(Vec4(getJSONArray(name), offset))
    else null
}
