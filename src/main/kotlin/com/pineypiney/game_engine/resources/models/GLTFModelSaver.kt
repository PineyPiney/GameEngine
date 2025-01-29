package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PBRMaterial
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.extension_functions.toBytes
import glm_.putInt
import glm_.vec4.Vec4
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GLTFModelSaver(val model: Model){

	val buffer = mutableListOf<Byte>()
	val bufferViews = JSONArray()
	val accessors = JSONArray()

	val materials = mutableSetOf<ModelMaterial>()

	fun saveGLB(file: File){

		val json = JSONObject()

		buildAssetJson(json)
		buildSceneJson(json)
		buildMeshesJson(json)
		buildMaterials(json)

		json.put("accessors", accessors)
		json.put("bufferViews", bufferViews)
		json.put("buffers", JSONArray().put(JSONObject().put("byteLength", buffer.size)))

		if(!file.exists()) {
			file.parentFile?.mkdirs()
			file.createNewFile()
		}


		val jsonStr = json.toString()
		val bytes = ByteArray(jsonStr.length + buffer.size + 28)

		// HEADER
		"glTF".toByteArray(Charsets.ISO_8859_1).copyInto(bytes)
		bytes.putInt(4, 2, false)
		bytes.putInt(8, bytes.size, false)

		// JSON
		bytes.putInt(12, jsonStr.length, false)
		"JSON".toByteArray(Charsets.ISO_8859_1).copyInto(bytes, 16)
		jsonStr.toByteArray(Charsets.ISO_8859_1).copyInto(bytes, 20)

		// BIN
		bytes.putInt(jsonStr.length + 20, buffer.size, false)
		("BIN".toByteArray(Charsets.ISO_8859_1) + 0).copyInto(bytes, jsonStr.length + 24)
		buffer.toByteArray().copyInto(bytes, jsonStr.length + 28)

		file.writeBytes(bytes)
	}

	fun buildAssetJson(parent: JSONObject): JSONObject{
		val j = JSONObject()
		j.put("generator", "Khronos glTF Piney's Game Engine")
		j.put("version", "2.0")
		parent.put("asset", j)
		return j
	}

	fun buildSceneJson(parent: JSONObject) {
		parent.put("scene", "0")

		val scene = JSONObject()
		scene.put("name", "scene")
		scene.put("nodes", JSONArray().put("0"))
		parent.put("scenes", JSONArray().put(scene))
	}

	fun buildMeshesJson(parent: JSONObject) {
		for(mesh in model.meshes){
			val meshJson = JSONObject()
			meshJson.put("name", mesh.id)

			val primitive = JSONObject()

			val attributes = JSONObject()
			val numVert = mesh.vertices.size

			attributes.put("POSITION", accessors.length())
			addAccessor("VEC3", FLOAT, numVert, bufferViews.length())
			addBufferView(mesh.vertices.flatMap { ByteData.vec32Bytes<Float>(it.position, false).toList() })

			attributes.put("NORMAL", accessors.length())
			addAccessor("VEC3", FLOAT, numVert, bufferViews.length())
			addBufferView(mesh.vertices.flatMap { ByteData.vec32Bytes<Float>(it.normal, false).toList() })

			attributes.put("TEXCOORD_0", accessors.length())
			addAccessor("VEC2", FLOAT, numVert, bufferViews.length())
			addBufferView(mesh.vertices.flatMap { ByteData.vec22Bytes<Float>(it.texCoord, false).toList() })

			if(mesh is ModelTangentMesh){
				attributes.put("TANGENT", accessors.length())
				addAccessor("VEC3", FLOAT, numVert, bufferViews.length())
				addBufferView(mesh.vertices.filterIsInstance<ModelTangentMesh.TangentMeshVertex>().flatMap { ByteData.vec32Bytes<Float>(it.tangent, false).toList() })
			}

			primitive.put("attributes", attributes)

			primitive.put("indices", accessors.length())
			addAccessor("SCALAR", USHORT, mesh.indices.size, bufferViews.length())
			addBufferView(mesh.indices.flatMap { it.toBytes(2) })

			var materialIndex = materials.indexOf(mesh.material)
			if(materialIndex == -1){
				materialIndex = materials.size
				materials.add(mesh.material)
			}
			primitive.put("material", materialIndex)

			meshJson.put("primitives", JSONArray().put(primitive))

			parent.put("meshes", JSONArray().put(meshJson))
		}
	}

	fun buildMaterials(parent: JSONObject){
		val materialsJson = JSONArray()
		for(material in materials){
			val matJson = JSONObject()
			matJson.put("name", material.name)
			matJson.put("doubleSided", "true")
			if(material is PBRMaterial){
				val pbrJson = JSONObject()
				if(material.baseColour != Vec4(1f)) pbrJson.put("baseColourFactor", material.baseColour.toFloatArray())
				if(material.metallicness != 1f) pbrJson.put("metallicFactor", material.metallicness)
				if(material.roughness != 1f) pbrJson.put("roughnessFactor", material.roughness)
				matJson.put("pbrMetallicRoughness", pbrJson)
			}

			materialsJson.put(matJson)
		}

		parent.put("materials", materialsJson)
	}

	fun addAccessor(type: String, primType: Int, count: Int, buffer: Int){
		val accessor = JSONObject()
		accessor.put("bufferView", buffer)
		accessor.put("componentType", primType)
		accessor.put("count", count)
		accessor.put("type", type)
		accessors.put(accessor)
	}

	fun addBufferView(data: List<Byte>){
		addBufferView(data.size, buffer.size)
		buffer.addAll(data)
	}

	fun addBufferView(size: Int, offset: Int){
		val view = JSONObject()
		view.put("buffer", "0")
		view.put("byteLength", size)
		view.put("byteOffset", offset)
		bufferViews.put(view)
	}

	fun reset(){
		buffer.clear()
		bufferViews.clear()
		accessors.clear()
		materials.clear()
	}

	companion object {
		const val BYTE = 5120
		const val UBYTE = 5121
		const val SHORT = 5122
		const val USHORT = 5123
		const val UINT = 5125
		const val FLOAT = 5126
	}
}