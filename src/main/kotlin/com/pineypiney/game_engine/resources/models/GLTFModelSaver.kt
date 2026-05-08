package com.pineypiney.game_engine.resources.models

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PBRMaterial
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.extension_functions.with
import glm_.putInt
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C
import java.io.File

class GLTFModelSaver(val model: Model){

	val buffer = mutableListOf<Byte>()
	val bufferViews = JsonArray()
	val accessors = JsonArray()

	val materials = mutableSetOf<ModelMaterial>()

	fun saveGLB(file: File){

		val json = JsonObject()

		buildAssetJson(json)
		buildSceneJson(json)
		buildMeshesJson(json)
		buildMaterials(json)


		json.with("accessors", accessors)
			.with("bufferViews", bufferViews)
			.with("buffers", JsonArray().with(JsonObject().with("byteLength", buffer.size)))

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

		// Json
		bytes.putInt(12, jsonStr.length, false)
		"Json".toByteArray(Charsets.ISO_8859_1).copyInto(bytes, 16)
		jsonStr.toByteArray(Charsets.ISO_8859_1).copyInto(bytes, 20)

		// BIN
		bytes.putInt(jsonStr.length + 20, buffer.size, false)
		("BIN".toByteArray(Charsets.ISO_8859_1) + 0).copyInto(bytes, jsonStr.length + 24)
		buffer.toByteArray().copyInto(bytes, jsonStr.length + 28)

		file.writeBytes(bytes)
	}

	fun buildAssetJson(parent: JsonObject): JsonObject {
		val j = JsonObject()
		j.with("generator", "Khronos glTF Piney's Game Engine")
			.with("version", "2.0")
			.with("asset", j)
		return j
	}

	fun buildSceneJson(parent: JsonObject) {
		parent.addProperty("scene", "0")

		val scene = JsonObject()
		scene.addProperty("name", "scene")
		scene.add("nodes", JsonArray().with("0"))
		parent.add("scenes", JsonArray().with(scene))
	}

	fun buildMeshesJson(parent: JsonObject) {
		for(mesh in model.meshes){
			val meshJson = JsonObject()
			meshJson.addProperty("name", mesh.id)

			val primitive = JsonObject()

			val attributes = JsonObject()
			val numVert = mesh.vertices.size

			attributes.addProperty("POSITION", accessors.size())
			addAccessor("VEC3", GL11C.GL_FLOAT, numVert, bufferViews.size())
			addBufferView(mesh.vertices.flatMap { ByteData.vec32Bytes(it.position, false).toList() })

			//attributes.addProperty("NORMAL", accessors.length())
			//addAccessor("VEC3", FLOAT, numVert, bufferViews.length())
			//addBufferView(mesh.vertices.flatMap { ByteData.vec32Bytes<Float>(it.normal, false).toList() })
//
			//attributes.addProperty("TEXCOORD_0", accessors.length())
			//addAccessor("VEC2", FLOAT, numVert, bufferViews.length())
			//addBufferView(mesh.vertices.flatMap { ByteData.vec22Bytes<Float>(it.texCoord, false).toList() })

			primitive.add("attributes", attributes)

			primitive.addProperty("indices", accessors.size())
			addAccessor("SCALAR", GL11C.GL_UNSIGNED_SHORT, mesh.indices.size, bufferViews.size())
			addBufferView(mesh.indices.flatMap { ByteData.int2Bytes(it, 2).toList() })

			var materialIndex = materials.indexOf(mesh.material)
			if(materialIndex == -1){
				materialIndex = materials.size
				materials.add(mesh.material)
			}
			primitive.addProperty("material", materialIndex)

			meshJson.add("primitives", JsonArray().with(primitive))

			parent.add("meshes", JsonArray().with(meshJson))
		}
	}

	fun buildMaterials(parent: JsonObject) {
		val materialsJson = JsonArray()
		for(material in materials){
			val matJson = JsonObject()
			matJson.addProperty("name", material.name)
			matJson.addProperty("doubleSided", "true")
			if(material is PBRMaterial){
				val pbrJson = JsonObject()

				if (material.baseColour != Vec4(1f)) pbrJson.add("baseColourFactor", JsonArray().with(material.baseColour.toFloatArray().asIterable()))
				if (material.metallicness != 1f) pbrJson.addProperty("metallicFactor", material.metallicness)
				if (material.roughness != 1f) pbrJson.addProperty("roughnessFactor", material.roughness)
				matJson.add("pbrMetallicRoughness", pbrJson)
			}

			materialsJson.add(matJson)
		}

		parent.with("materials", materialsJson)
	}

	fun addAccessor(type: String, primType: Int, count: Int, buffer: Int){
		val accessor = JsonObject()
		accessor.addProperty("bufferView", buffer)
		accessor.addProperty("componentType", primType)
		accessor.addProperty("count", count)
		accessor.addProperty("type", type)
		accessors.add(accessor)
	}

	fun addBufferView(data: List<Byte>){
		addBufferView(data.size, buffer.size)
		buffer.addAll(data)
	}

	fun addBufferView(size: Int, offset: Int){
		val view = JsonObject()
		view.addProperty("buffer", "0")
		view.addProperty("byteLength", size)
		view.addProperty("byteOffset", offset)
		bufferViews.add(view)
	}

	fun reset(){
		buffer.clear()
		bufferViews.removeAll { true }
		accessors.removeAll { true }
		materials.clear()
	}
}