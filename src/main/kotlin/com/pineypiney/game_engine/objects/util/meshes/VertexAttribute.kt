package com.pineypiney.game_engine.objects.util.meshes

import com.pineypiney.game_engine.util.extension_functions.*
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import org.lwjgl.opengl.GL11C
import java.nio.ByteBuffer

class VertexAttribute<T>(val name: String, val size: Int, val type: Int, val get: (ByteBuffer, Int) -> T, val set: (ByteBuffer, Int, T) -> Unit) {

	operator fun component1() = size
	operator fun component2() = type

	companion object {
		val POSITION2D = VertexAttribute<Vec2>("Position2D", 2, GL11C.GL_FLOAT, ByteBuffer::getVec2, ByteBuffer::putVec2)
		val POSITION = VertexAttribute<Vec3>("Position", 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3)
		val TEX_COORD = VertexAttribute<Vec2>("TexCoord", 2, GL11C.GL_FLOAT, ByteBuffer::getVec2, ByteBuffer::putVec2)
		val NORMAL = VertexAttribute<Vec3>("Normal", 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3)
		val TANGENT = VertexAttribute<Vec3>("Tangent", 3, GL11C.GL_FLOAT, ByteBuffer::getVec3, ByteBuffer::putVec3)
		val BONE_IDS = VertexAttribute<Vec4i>("BoneIds", 4, GL11C.GL_INT, ByteBuffer::getVec4i, ByteBuffer::putVec4i)
		val BONE_WEIGHTS = VertexAttribute<Vec4>("BoneWeights", 4, GL11C.GL_FLOAT, ByteBuffer::getVec4, ByteBuffer::putVec4)
	}
}