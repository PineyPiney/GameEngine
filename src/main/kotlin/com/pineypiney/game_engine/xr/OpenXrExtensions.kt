package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4s
import org.lwjgl.openxr.*

fun XrQuaternionf.set(value: Quat) {
	set(value.x, value.y, value.z, value.w)
}

fun XrQuaternionf.glm() = Quat(w(), x(), y(), z())

fun Quat.put(value: XrQuaternionf) {
	value.set(x, y, z, w)
}

fun XrVector3f.set(value: Vec3) {
	set(value.x, value.y, value.z)
}

fun XrVector2f.glm() = Vec2(x(), y())

fun XrVector3f.glm() = Vec3(x(), y(), z())

fun XrVector4f.glm() = Vec4(x(), y(), z(), w())

fun XrVector4sFB.glm() = Vec4s(x(), y(), z(), w())

fun Vec3.put(value: XrVector3f) {
	value.set(x, y, z)
}

fun XrPosef.set(rotation: Quat, position: Vec3){
	orientation(rotation::put)
	`position$`(position::put)
}

fun XrPosef.reset(){
	orientation(Quat()::put)
	`position$`(Vec3()::put)
}

fun XrPosef.mat4(res: Mat4 = I): Mat4 {
	return I.translate(`position$`().glm()).times(orientation().glm().toMat4(), res)
}

fun XrRect2Di.set(x: Int, y: Int, width: Int, height: Int): XrRect2Di {
	offset().set(x, y)
	extent().set(width, height)
	return this
}