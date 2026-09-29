package com.pineypiney.game_engine.util.jna

import com.sun.jna.Pointer
import glm_.quat.Quat
import glm_.vec2.Vec2b
import glm_.vec2.Vec2s
import glm_.vec3.Vec3
import glm_.vec4.Vec4s

fun Pointer.getVec2b(offset: Long) = Vec2b { getByte(offset + it) }
fun Pointer.getVec2s(offset: Long) = Vec2s { getShort(offset + it * 2) }
fun Pointer.getVec4s(offset: Long) = Vec4s { getShort(offset + it * 2) }
fun Pointer.getQuat(offset: Long) = Quat(getFloat(offset + 12), getFloat(offset), getFloat(offset + 4), getFloat(offset + 8))
fun Pointer.getVec3(offset: Long) = Vec3 { getFloat(offset + it * 4) }