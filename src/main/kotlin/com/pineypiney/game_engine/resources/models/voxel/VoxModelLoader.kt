package com.pineypiney.game_engine.resources.models.voxel

import com.pineypiney.game_engine.rendering.meshes.IndicesMeshBuilder
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelMesh
import com.pineypiney.game_engine.resources.readInt
import com.pineypiney.game_engine.resources.readString
import com.pineypiney.game_engine.util.BitMap3D
import com.pineypiney.game_engine.util.extension_functions.getOrSet
import glm_.vec3.Vec3i
import glm_.vec4.Vec4i
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class VoxModelLoader {
	fun loadVoxModel(name: String, stream: InputStream): Model {
		stream.readString(4)		// "VOX "
		stream.readInt(false)	// Version (150 or 200)
		val models = mutableListOf<VoxelModel>()
		val rgba = Array(255){ 0 }
		val nodes = mutableListOf<VoxNode>()
		loadVoxChunk(stream, models, rgba, nodes)

		for(node in nodes){
			if(node is VoxNode.TransformNode){
				val target = nodes.find { it.id == node.target }
				if(target is VoxNode.ShapeNode){
					for(i in target.models){
						models[i].name = node.name
						models[i].translation += (node.frames.first().first)
					}
				}
			}
		}


		val meshes = models.map { it.generateMesh(rgba) }
		return Model(name, meshes.toTypedArray())
	}

	@Suppress("UNUSED_VARIABLE", "UNUSED")
	fun loadVoxChunk(stream: InputStream, models: MutableList<VoxelModel>, rgba: Array<Int>, nodes: MutableList<VoxNode>){
		val chunkType = stream.readString(4)
		val chunkSize = stream.readInt(false)
		val childrenSize = stream.readInt(false)

		val chunk = stream.readNBytes(chunkSize).inputStream()
		val children = stream.readNBytes(childrenSize).inputStream()

		when(chunkType){
			"MAIN" -> {
				while(children.available() > 0) loadVoxChunk(children, models, rgba, nodes)
			}
			"SIZE" -> {
				val z = chunk.readInt(false)
				models.add(VoxelModel("Model ${models.size}", chunk.readInt(false), chunk.readInt(false), z))
			}
			"XYZI" -> {
				val model = models.last()
				val numVoxels = chunk.readInt(false)
				while(model.voxels.size < numVoxels && chunk.available() >= 4) {
					val zxyi = chunk.readInt()
					model.addVoxel(((zxyi and 0xff0000) shl 8) or ((zxyi and 0xff00) shl 8) or ((zxyi shr 16) and 0xff00) or (zxyi and 255))
				}
				model.optimise()
			}
			"RGBA" -> {
				for(i in 0..254) rgba[i] = chunk.readInt()
			}
			// Transform Node
			"nTRN" -> {
				// This will point to a Shape Node
				val nodeID = chunk.readInt(false)
				val nodeInfo = loadDict(chunk)

				val childIndex = chunk.readInt(false)
				val reserved = chunk.readInt(false)
				val layer = chunk.readInt(false)
				val numFrames = chunk.readInt(false)

				val node = VoxNode.TransformNode(nodeID, childIndex, nodeInfo)

				for(i in 0..<numFrames){
					node.addFrame(loadDict(chunk))
				}
				nodes.add(node)
			}
			// Group node
			"nGRP" -> {
				val nodeID = chunk.readInt(false)
				loadDict(chunk)

				val numChildren = chunk.readInt(false)
				for(i in 0..<numChildren) chunk.readInt(false)
			}
			// Shape Node
			"nSHP" -> {
				val nodeID = chunk.readInt(false)
				val dict = loadDict(chunk)

				val numModels = chunk.readInt(false)
				val models = Array(numModels){ 0 }
				for(i in 0..<numModels){
					models[i] = chunk.readInt(false)
					val frameDict = loadDict(chunk)
				}
				nodes.add(VoxNode.ShapeNode(nodeID, models))
			}
			"LAYR" -> {
				val layer = chunk.readInt(false)
				val dict = loadDict(chunk)
				val reserver = chunk.readInt(false)
			}
		}

		chunk.close()
		children.close()
	}

	fun loadDict(stream: InputStream): Map<String, String>{
		val numEntries = stream.readInt(false)
		if(numEntries == 0) return emptyMap()
		val map = mutableMapOf<String, String>()
		(0..<numEntries).forEach { _ ->
			val key = stream.readString(stream.readInt(false))
			val value = stream.readString(stream.readInt(false))
			map[key] = value
		}
		return map
	}

	class VoxelModel(var name: String, val size: Vec3i){

		constructor(name: String, x: Int, y: Int, z: Int): this(name, Vec3i(x, y, z))
		val voxels = mutableSetOf<Int>()
		val translation: Vec3i = size / -2

		fun addVoxel(int: Int) = voxels.add(int)

		fun optimise(){
			var min = Vec3i(255)
			var max = Vec3i(0)

			for(voxel in voxels){
				val (x, y, z) = getValues(voxel)
				min = Vec3i(min(min.x, x), min(min.y, y), min(min.z, z))
				max = Vec3i(max(max.x, x), max(max.y, y), max(max.z, z))
			}

			val newSize = max + 1 - min
			val newVoxels = voxels.map { voxel ->
				val (x, y, z, i) = getValues(voxel)
				createVoxel(x - min.x, y - min.y, z - min.z, i)
			}

			translation += min
			size(newSize)
			voxels.clear()
			voxels.addAll(newVoxels)
		}

		fun toColourMaps(): Map<UByte, BitMap3D>{
			val maps = mutableMapOf<UByte, BitMap3D>()
			for(voxel in voxels){
				val (x, y, z, i) = getValues(voxel)
				val map = maps.getOrSet(i.toUByte()){ BitMap3D(size) }
				map.or(x, y, z)
			}
			return maps
		}

		fun generateMesh(colours: Array<Int>): ModelMesh {

			val builder = IndicesMeshBuilder(VertexAttribute.POSITION, VertexAttribute.COLOUR)

			val colourMaps = toColourMaps()
			for((colourIndex, map) in colourMaps){
				val colour = colours[colourIndex.toInt() - 1]
				val zSlices = (0..<map.z).map { map.sliceXY(it) }
				val ySlices = (0..<map.y).map { map.sliceXZ(it) }
				val xSlices = (0..<map.x).map { map.sliceYZ(it) }
//				val quads = mutableListOf<Pair<Vec3i, Vec3i>>()

				for(z in 0..<zSlices.size){
					val backSlice = if(z == 0) zSlices.first().copy()
					else zSlices[z].andNot(zSlices[z - 1])
					val frontSlice = if(z == zSlices.size - 1) zSlices.last().copy()
					else zSlices[z].andNot(zSlices[z + 1])
					for(rect in backSlice.greedyMesh()){
						rect += Vec4i(translation.x, translation.y, translation.x, translation.y)
//						quads.add(Vec3i(rect.z, rect.y, z + translation.z) to Vec3i(rect.x, rect.w, z + translation.z))
						builder.quad()
							.vertex(rect.z, rect.y, z + translation.z).rgba(colour)
							.vertex(rect.x, rect.y, z + translation.z).rgba(colour)
							.vertex(rect.x, rect.w, z + translation.z).rgba(colour)
							.vertex(rect.z, rect.w, z + translation.z).rgba(colour)
					}
					for(rect in frontSlice.greedyMesh()){
						rect += Vec4i(translation.x, translation.y, translation.x, translation.y)
//						quads.add(Vec3i(rect.x, rect.y, z + translation.z + 1) to Vec3i(rect.z, rect.w, z + translation.z + 1))
						builder.quad()
							.vertex(rect.x, rect.y, z + translation.z + 1).rgba(colour)
							.vertex(rect.z, rect.y, z + translation.z + 1).rgba(colour)
							.vertex(rect.z, rect.w, z + translation.z + 1).rgba(colour)
							.vertex(rect.x, rect.w, z + translation.z + 1).rgba(colour)
					}
				}

				for(y in 0..<ySlices.size){
					val bottomSlice = if(y == 0) ySlices.first().copy()
					else ySlices[y].andNot(ySlices[y - 1])
					val topSlice = if(y == ySlices.size - 1) ySlices.last().copy()
					else ySlices[y].andNot(ySlices[y + 1])
					for(rect in bottomSlice.greedyMesh()){
						rect += Vec4i(translation.x, translation.z, translation.x, translation.z)
						builder.quad()
							.vertex(rect.x, y + translation.y, rect.y).rgba(colour)
							.vertex(rect.z, y + translation.y, rect.y).rgba(colour)
							.vertex(rect.z, y + translation.y, rect.w).rgba(colour)
							.vertex(rect.x, y + translation.y, rect.w).rgba(colour)
					}
					for(rect in topSlice.greedyMesh()){
						rect += Vec4i(translation.x, translation.z, translation.x, translation.z)
						builder.quad()
							.vertex(rect.x, y + translation.y + 1, rect.y).rgba(colour)
							.vertex(rect.x, y + translation.y + 1, rect.w).rgba(colour)
							.vertex(rect.z, y + translation.y + 1, rect.w).rgba(colour)
							.vertex(rect.z, y + translation.y + 1, rect.y).rgba(colour)
					}
				}

				for(x in 0..<xSlices.size){
					val leftSlice = if(x == 0) xSlices.first().copy()
					else xSlices[x].andNot(xSlices[x - 1])
					val rightSlice = if(x == xSlices.size - 1) xSlices.last().copy()
					else xSlices[x].andNot(xSlices[x + 1])
					for(rect in leftSlice.greedyMesh()){
						rect += Vec4i(translation.y, translation.z, translation.y, translation.z)
						builder.quad()
							.vertex(x + translation.x, rect.x, rect.y).rgba(colour)
							.vertex(x + translation.x, rect.x, rect.w).rgba(colour)
							.vertex(x + translation.x, rect.z, rect.w).rgba(colour)
							.vertex(x + translation.x, rect.z, rect.y).rgba(colour)
					}
					for(rect in rightSlice.greedyMesh()){
						rect += Vec4i(translation.y, translation.z, translation.y, translation.z)
						builder.quad()
							.vertex(x + translation.x + 1, rect.x, rect.w).rgba(colour)
							.vertex(x + translation.x + 1, rect.x, rect.y).rgba(colour)
							.vertex(x + translation.x + 1, rect.z, rect.y).rgba(colour)
							.vertex(x + translation.x + 1, rect.z, rect.w).rgba(colour)
					}
				}
			}

			return builder.buildModel(name)
		}

		companion object {
			fun getValues(voxel: Int): Vec4i{
				return Vec4i((voxel shr 24) and 255, (voxel shr 16) and 255, (voxel shr 8) and 255, voxel and 255)
			}

			fun createVoxel(x: Int, y: Int, z: Int, i: Int): Int {
				return ((x and 255) shl 24) or ((y and 255) shl 16) or ((z and 255) shl 8) or (i and 255)
			}
		}
	}
}