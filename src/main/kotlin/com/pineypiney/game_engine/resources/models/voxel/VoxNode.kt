package com.pineypiney.game_engine.resources.models.voxel

import glm_.quat.Quat
import glm_.vec3.Vec3i

open class VoxNode(val id: Int) {

	class TransformNode(id: Int, val target: Int, val name: String, val frames: MutableList<Triple<Vec3i, Quat, Int>>): VoxNode(id){
		constructor(id: Int, target: Int, nodeDict: Map<String, String>): this(id, target, nodeDict["_name"] ?: "", mutableListOf())

		fun addFrame(frameDict: Map<String, String>){
			val translation = frameDict["_t"]?.let {
				Vec3i(it.split(' ').map(Integer::parseInt))
			} ?: Vec3i(0)
			val rotation = Quat.identity
			val frameIndex = frameDict["_f"]?.toInt() ?: 0
			frames.add(Triple(translation, rotation, frameIndex))
		}
	}
	class ShapeNode(id: Int, val models: Array<Int>): VoxNode(id)
//	class GroupNode(id: Int, val children: Set<Int>): VoxNode(id)
}