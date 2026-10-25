package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.models.Bone
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.extension_functions.hasAll
import glm_.hasnt
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec4.Vec4i
import kool.map
import kool.toIntArray
import org.lwjgl.openxr.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class HandTracker(val hand: XrHandTrackerEXT, val openxr: OpenXrManager) : Deletable {

	val joints = XrHandJointLocationEXT.calloc(EXTHandTracking.XR_HAND_JOINT_COUNT_EXT)

	lateinit var rootBone: Bone private set

	lateinit var mesh: ModelMesh private set

	fun poll(stack: MemoryStack, session: OpenXrSession, time: Long){

		val locateInfo = XrHandJointsLocateInfoEXT.calloc(stack).`type$Default`()
			.baseSpace(session.space).time(time)

		if(EXTHandJointsMotionRange.XR_EXT_HAND_JOINTS_MOTION_RANGE_EXTENSION_NAME in OpenXrManager.enabledExtensions){
			val rangeInfo = XrHandJointsMotionRangeInfoEXT.calloc(stack).`type$Default`()
				.handJointsMotionRange(EXTHandJointsMotionRange.XR_HAND_JOINTS_MOTION_RANGE_UNOBSTRUCTED_EXT)
			locateInfo.next(rangeInfo)
		}

		val locations = XrHandJointLocationsEXT.calloc(stack).`type$Default`()
			.jointLocations(joints)

		XrUtil.processResult(EXTHandTracking.xrLocateHandJointsEXT(hand, locateInfo, locations), "Failed to location hand joints")
	}

	fun getMesh(factory: ResourceFactory): ModelMesh {
		if(FBHandTrackingMesh.XR_FB_HAND_TRACKING_MESH_EXTENSION_NAME !in OpenXrManager.enabledExtensions){
			GameEngineI.warn(FBHandTrackingMesh.XR_FB_HAND_TRACKING_MESH_EXTENSION_NAME + " Not enabled, could not create hand mesh")
			return ModelMesh.missing
		}
		val meshStruct = XrHandTrackingMeshFB.calloc().`type$Default`()
		XrUtil.processResult(FBHandTrackingMesh.xrGetHandMeshFB(hand, meshStruct), "Failed to get Hand Mesh Data")
		val positions = XrVector3f.calloc(meshStruct.vertexCountOutput())
		val normals = XrVector3f.calloc(meshStruct.vertexCountOutput())
		val uvs = XrVector2f.calloc(meshStruct.vertexCountOutput())
		val boneIndices = XrVector4sFB.calloc(meshStruct.vertexCountOutput())
		val boneWeights = XrVector4f.calloc(meshStruct.vertexCountOutput())

		val joints = XrPosef.calloc(meshStruct.jointCountOutput())
		val jointRadii = MemoryUtil.memAllocFloat(meshStruct.jointCountOutput())
		val jointParents = MemoryUtil.memAllocInt(meshStruct.jointCountOutput())

		val indices = MemoryUtil.memAllocShort(meshStruct.indexCountOutput())

//		val deletion = DeletionQueue().push(positions).push(normals).push(uvs).push(boneIndices).push(boneWeights)
//			.push(joints).push(jointRadii, MemoryUtil::memFree).push(jointParents, MemoryUtil::memFree)
//			.push(indices, MemoryUtil::memFree)

		meshStruct
			.vertexCapacityInput(meshStruct.vertexCountOutput())
			.vertexPositions(positions).vertexNormals(normals).vertexUVs(uvs)
			.vertexBlendIndices(boneIndices).vertexBlendWeights(boneWeights)

			.jointCapacityInput(meshStruct.jointCountOutput())
			.jointBindPoses(joints).jointRadii(jointRadii).jointParents(jointParents)

			.indexCapacityInput(meshStruct.indexCountOutput()).indices(indices)
		XrUtil.processResult(FBHandTrackingMesh.xrGetHandMeshFB(hand, meshStruct), "Failed to get Hand Mesh Data")

		val vertices = List(meshStruct.vertexCountOutput()) { MeshVertex(setOf(
			VertexAttribute.POSITION.pair(positions[it].glm()),
			VertexAttribute.NORMAL.pair(normals[it].glm()),
			VertexAttribute.TEX_COORD.pair(uvs[it].glm()),
			VertexAttribute.BONE_IDS.pair(Vec4i(boneIndices[it].glm())),
			VertexAttribute.BONE_WEIGHTS.pair(boneWeights[it].glm()),
		)) }
		val meshIndices = indices.map(Short::toInt).toIntArray()


		val orderedParents = jointParents.toIntArray().withIndex().sortedBy{
			if(it.value > meshStruct.jointCountOutput()) 0 else it.value
		}

		val bones = Array<Bone?>(meshStruct.jointCountOutput()) { null }
		for((i, parent) in orderedParents){
			val parentBone = bones.getOrNull(parent)
			val relative = if(parentBone == null) joints[i].mat4() else joints[parent].mat4().inverse() * joints[i].mat4()
			bones[i] = Bone(parentBone, i, "Hand Bone $i", "Hand Bone", relative)
			parentBone?.addChild(bones[i]!!)
		}
		meshStruct.free()
		rootBone = bones[orderedParents.first().index]!!


//			deletion.flush()
		return factory.createModelMesh("Hand Mesh", vertices, meshIndices)
	}

	fun updateJoints(){
		for((i, joint) in joints.withIndex()){
			if(joint.locationFlags() hasnt ALL_FLAGS) continue
			val bone = rootBone.getChild(i) ?: continue
			val parentBone = bone.parent
			val parentJoint = parentBone?.let { joints[parentBone.id] }
			if(parentJoint != null && parentJoint.locationFlags() hasnt ALL_FLAGS) continue
			val relative = if(parentJoint == null) joint.pose().mat4() else parentJoint.pose().mat4().inverse() * joint.pose().mat4()
			val eee = bone.defaultRelativeTransform.inverse() * relative

			bone.translation = eee.getTranslation()
			bone.rotation = eee.getRotation()
		}
		rootBone.updateModel()
	}

	fun getBoneTransforms(): Array<Mat4> {
		return rootBone.getAllChildrenSorted().map {
			it.getMeshTransform()
		}.toTypedArray()
	}

	fun jointActive(joint: Int) = joints[joint].locationFlags() hasAll ALL_FLAGS

	fun getRotation(joint: Int): Quat {
		return joints[joint].pose().orientation().glm()
	}

	override fun delete() {
		joints.free()
		XrUtil.processResult(EXTHandTracking.xrDestroyHandTrackerEXT(hand), "Failed to delete Hand Tracker")
	}

	companion object {
		val ALL_FLAGS = (XR10.XR_SPACE_LOCATION_ORIENTATION_VALID_BIT or
				XR10.XR_SPACE_LOCATION_POSITION_VALID_BIT or
				XR10.XR_SPACE_LOCATION_ORIENTATION_TRACKED_BIT or
				XR10.XR_SPACE_LOCATION_POSITION_TRACKED_BIT).toLong()

	}
}