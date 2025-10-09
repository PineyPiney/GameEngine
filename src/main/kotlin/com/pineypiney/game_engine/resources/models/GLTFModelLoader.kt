package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.models.animations.BoneState
import com.pineypiney.game_engine.resources.models.animations.KeyFrame
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PBRMaterial
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.textures.TextureParameters
import com.pineypiney.game_engine.util.exceptions.ModelParseException
import com.pineypiney.game_engine.util.extension_functions.*
import com.pineypiney.game_engine.util.maths.Collider2D
import com.pineypiney.game_engine.util.maths.Collider3D
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.*
import glm_.mat2x2.Mat2
import glm_.mat3x3.Mat3
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.*
import glm_.vec3.*
import glm_.vec4.*
import kool.count
import kool.toBuffer
import org.json.JSONArray
import org.json.JSONObject
import org.lwjgl.opengl.GL11C
import unsigned.ui
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLTFModelLoader(val loader: ModelLoader) {

	fun loadModel(fileName: String, json: JSONObject, buffers: List<ByteArray>): Model {

		if (json.isEmpty) return Model.brokeModel

		val bufferViewsJson = json.getJSONArray("bufferViews")
		val bufferViews = mutableListOf<ByteArray>()
		bufferViewsJson.forEachObject { o, _ ->
			val buffer = buffers[o.getInt("buffer")]
			val length = o.getInt("byteLength")
			val offset = o.getInt("byteOffset")


			bufferViews.add(buffer.copyOfRange(offset, offset + length))
		}

		val accessorsJson = json.getJSONArray("accessors")
		val accessors = mutableListOf<Array<Any>>()
		for ((_, o) in accessorsJson.objects) {
			val view = bufferViews[o.getInt("bufferView")].toBuffer()
			val type = DataType.entries.first { it.value == o.getInt("componentType") }
			val matrix = DataMatrix.valueOf(o.getString("type"))
			val size = type.bytes * matrix.size
			val count = o.getInt("count")

			if (view.count() < size * count) {
				accessors.add(arrayOf())
				continue
			}

			view.order(ByteOrder.LITTLE_ENDIAN)

			accessors.add(Array(count) { matrix.make(view, it * size, type) })
		}

		val textures = loadTextures(json, bufferViews)


        // Load Materials

		val materialsJson = json.getJSONArray("materials")
		val materials = mutableListOf<PBRMaterial>()

		materialsJson.forEachObject { materialJson, _ ->
			val name = materialJson.getString("name")
			val materialTextures = mutableMapOf<String, Texture>()

			fun getTextureIndex(json: JSONObject, name: String) = (json.getOrNull(name) as? JSONObject)?.getInt("index") ?: -1

			if(materialJson.has("pbrMetallicRoughness")) {
				val pbrJson = materialJson.getJSONObject("pbrMetallicRoughness")
				val baseColour = pbrJson.getVec4("baseColourFactor") ?: Vec4(1f)
				textures.getOrNull(getTextureIndex(pbrJson, "baseColorTexture"))?.let { materialTextures["baseColour"] = it }

				val metallicFactor = pbrJson.getFloatOrNull("metallicFactor") ?: 1f
				val roughnessFactor = pbrJson.getFloatOrNull("roughnessFactor") ?: 1f
				textures.getOrNull(getTextureIndex(pbrJson, "metallicRoughnessTexture"))?.let { materialTextures["metallicRoughness"] = it }

				textures.getOrNull(getTextureIndex(materialJson, "normalTexture"))?.let { materialTextures["normals"] = it }
				textures.getOrNull(getTextureIndex(materialJson, "occlusionTexture"))?.let { materialTextures["occlusion"] = it }
				val emissiveFactor = materialJson.getFloatOrNull("emissiveFactor") ?: 1f
				textures.getOrNull(getTextureIndex(materialJson, "emissiveTexture"))?.let { materialTextures["emissive"] = it }




				materials.add(PBRMaterial(name, materialTextures, baseColour, metallicFactor, roughnessFactor, emissiveFactor))
			}

			else materials.add(PBRMaterial(name, emptyMap()))
		}


        // Load Meshes

		val meshesJson = json.getJSONArray("meshes")
		val meshes = mutableListOf<ModelMesh>()

        meshesJson.forEachObject { meshJson, _ ->
            val name = meshJson.getString("name")
            val primitives = meshJson.getJSONArray("primitives")
            for ((_, primitive) in primitives.objects) {
                loadPrimitive(fileName, name, primitive, meshes, accessors, materials)
            }
        }


        // Bones

        val bones = mutableSetOf<Bone>()
        if(json.has("nodes") and json.has("skins")) {
            val skinJson = json.getJSONArray("skins")
            skinJson.forEachObject { skin, _ ->
                loadBones(fileName, json.getJSONArray("nodes"), skin, bones)
            }
        }


		// Animations

		val animations = mutableListOf<ModelAnimation>()

		val animationsJson = if (json.has("animations")) json.getJSONArray("animations") else null
		animationsJson?.forEachObject { animationJson, _ ->
			val name = animationJson.getString("name")
			val states = mutableMapOf<Float, MutableList<BoneState>>()

			val samplers = animationJson.getJSONArray("samplers").mapObjects { samplerJson, si ->
				val times = accessors[samplerJson.getInt("input")].map { it as Number }
				val values = accessors[samplerJson.getInt("output")]
				val pairs = mutableMapOf<Float, Any>()
				times.forEachIndexed { index, number -> pairs[number.f] = values[index] }
				si to pairs.toMap()
			}.toMap()

			for ((_, channelJson) in animationJson.getJSONArray("channels").objects) {
				val sampler = samplers[channelJson.getInt("sampler")] ?: continue
				val target = channelJson.getJSONObject("target")
				val node = json.getJSONArray("nodes").getJSONObject(target.getInt("node"))
				val mesh = node.getString("name")
				val path = target.getString("path")

				for ((t, v) in sampler) {
                    val frameStates = states.getOrSet(t){ mutableListOf() }
                    val nodeState = frameStates.getOrSet(mesh, BoneState::parentId){ BoneState(mesh, Vec3(), Quat()) }
                    when(path) {
                        "translation" -> nodeState.translation = v as Vec3
                        "rotation" -> nodeState.rotation = Quat(v as Vec4)
                    }
				}
			}

			val frames = states.map { (t, s) -> KeyFrame(t, s) }

			animations.add(ModelAnimation(name, frames.toTypedArray()))
		}



		val min = Vec3(Float.POSITIVE_INFINITY)
		val max = Vec3(Float.NEGATIVE_INFINITY)
		for (mesh in meshes) {
			for (vertex in mesh.vertices) {
				val p = vertex.position
				if (p.x < min.x) min.x = p.x
				if (p.y < min.y) min.y = p.y
				if (p.z < min.z) min.z = p.z
				if (p.x > max.x) max.x = p.x
				if (p.y > max.y) max.y = p.y
				if (p.z > max.z) max.z = p.z
			}
		}

		val collider =
			if (min.z == max.z) Collider2D(Rect2D(Vec2(min), Vec2(max - min)))
			else Collider3D(Cuboid((min + max) * .5f, Quat.identity, max - min))
		return Model(fileName, meshes.toTypedArray(), bones.firstOrNull(), animations.toTypedArray(), collider)
	}

    fun loadPrimitive(fileName: String, name: String, primitive: JSONObject, meshes: MutableCollection<ModelMesh>, accessors: List<Array<Any>>, materials: List<ModelMaterial>){

        val attributes = primitive.getJSONObject("attributes")
        val attributeMap = mutableMapOf<VertexAttribute<*>, Array<Any>>()

        val pos = attributes.getInt("POSITION")
        attributeMap[VertexAttribute.POSITION] = accessors[pos]
        val nor = attributes.getInt("NORMAL")
        attributeMap[VertexAttribute.NORMAL] = accessors[nor]
        val tex = attributes.getInt("TEXCOORD_0")
        attributeMap[VertexAttribute.TEX_COORD] = accessors[tex]

        val tan = attributes.getIntOrNull("TANGENT")
        if(tan != null) {
            if(accessors[tan].first() is Vec3) attributeMap[VertexAttribute.TANGENT] = accessors[tan]
            else if(accessors[tan].first() is Vec4) attributeMap[VertexAttribute.TANGENT_HANDED] = accessors[tan]
        }

        if(attributes.has("JOINTS_0") and attributes.has("WEIGHTS_0")){
            attributeMap[VertexAttribute.BONE_IDS] = accessors[attributes.getInt("JOINTS_0")]
            attributeMap[VertexAttribute.BONE_WEIGHTS] = accessors[attributes.getInt("WEIGHTS_0")]
        }


        val indices = primitive.getInt("indices")
        val material = primitive.getIntOrNull("material") ?: -1

        val indArray = accessors[indices].map { (it as Number).i }

        // Blender exports tangent as a VEC4, where the w components is the handedness of the tangent/bitangent
        // https://blender.stackexchange.com/questions/220756/why-does-blender-output-vec4-tangents-for-gltf#comment372839_220756

        // Check the arrays are all as large as the max index, otherwise log an error
        val maxIndex = indArray.max()
        for((att, arr) in attributeMap) {
            if (arr.size < maxIndex) {
                GameEngineI.error("$att in primitive $name of mesh $name in GLTF model $fileName is too small. It has ${arr.size} entries, but the index attribute requires at least $maxIndex entries")
                continue
            }
        }

        val vertices = Array(maxIndex + 1) {

            val values = attributeMap.map { (att, arr) ->
                try {
                    VertexAttribute.Pair(att, arr, it)
                }
                catch (_: ClassCastException) {
                    GameEngineI.logger.warn("Failed to load primitive $name due to attribute $att being type ${arr[it].javaClass.name}, which cannot be used for this attribute")
                    return
                }
            }.toSet()
			MeshVertex(values)

        }
        meshes.add(ModelMesh(name, vertices, indArray.toIntArray(), material = materials.getOrElse(material){ PBRMaterial.default }))
    }

    fun loadBones(fileName: String, nodesJson: JSONArray, boneJson: JSONObject, bones: MutableSet<Bone>) {
//        val bindMatrix = boneJson.getIntOrNull("inverseBindMatrices")
        val jointIndices = boneJson.getJSONArray("joints").map { it as Int }

        bones.add(loadBone(nodesJson, jointIndices.first(), jointIndices, null))
    }

    fun loadBone(nodesJson: JSONArray, boneIndex: Int, indices: List<Int>, parent: Bone?): Bone {
        val nodeJson = nodesJson.getJSONObject(boneIndex)
        val boneName =  nodeJson.getStringOrNull("name") ?: "bone_$boneIndex"

        val translation = nodeJson.getVec3("translation") ?: Vec3()
        val rotation = nodeJson.getQuat("rotation") ?: Quat()
        // Rounding and renormalising the quat stops tiny values from being carried over when converting between Mat4 and Quat
        val roundedRotation = Quat(Vec4 { rotation[it].round(4) }.normalize())
        val scale = nodeJson.getVec3("scale") ?: Vec3(1f)

        val bone = Bone(parent, indices.indexOf(boneIndex), boneName, boneName, (Mat4().translate(translation) * roundedRotation.toMat4()).scale(scale))

        if(nodeJson.has("children")) {
            for (f in nodeJson.getJSONArray("children")) {
                bone.addChild(loadBone(nodesJson, f as Int, indices, bone))
            }
        }

        return bone
    }

    fun loadTextures(json: JSONObject, bufferViews: List<ByteArray>): List<Texture>{
        if(!json.has("samplers")) return emptyList()
        val samplersJson = json.getJSONArray("samplers")
        val samplers = mutableListOf<TextureParameters>()
        for(i in 0..<samplersJson.length()) samplers.add(samplerType(samplersJson.getJSONObject(i)))

        val imagesJson = json.getJSONArray("images")
        val images = mutableListOf<Triple<String, ByteArray, Boolean>>()
        for(i in 0..<imagesJson.length()){
            val imageJson = imagesJson.getJSONObject(i)
            if(imageJson.has("uri")){
                images.add(Triple("Model Image $i", ByteArray(0), false))
            }
            else{
                val name = imageJson.getStringOrNull("name") ?: "Model Image $i"
                val imageData = bufferViews[imageJson.getInt("bufferView")]
                val png = imageJson.getString("mimeType") == "image/png"
                images.add(Triple(name, imageData, png))
            }
        }

        val texturesJson = json.getJSONArray("textures")
        val textures = mutableListOf<Texture>()
        for(i in 0..<texturesJson.length()){
            val textureJson = texturesJson.getJSONObject(i)
            val sampler = samplers[textureJson.getInt("sampler")]
            val image = images[textureJson.getInt("source")]
            textures.add(Texture(image.first, TextureLoader.loadTextureFromStream(image.first, image.second.inputStream(), sampler)))
        }
        return textures
    }


	fun loadGLTFFile(fileName: String, stream: InputStream): Model {
		val json = JSONObject(stream.readAllBytes().toString(Charsets.UTF_8))
		val buffersJson = json.getJSONArray("buffers")
		val buffers = mutableListOf<ByteArray>()
		for (i in 0..<buffersJson.length()) {
			val bufferLocation = buffersJson.getJSONObject(i).getString("uri")
			buffers.add(loadBinFile(fileName.substringBeforeLast('/') + "/" + bufferLocation))
		}
		return loadModel(fileName, json, buffers)
	}

	fun loadBinFile(name: String): ByteArray {
		val stream = loader.currentStreams[name] ?: return ByteArray(0)
		return stream.readAllBytes()
	}

	// https://docs.fileformat.com/3d/glb/ Praise the lord
	fun loadGLBFile(fileName: String, stream: InputStream): Model {
		stream.readNBytes(12) // Header
		var json = JSONObject()
		val buffers = mutableListOf<ByteArray>()
		while (stream.available() != 0) {
			val chunkHeader = stream.readNBytes(8) ?: break
			val size = chunkHeader.getInt(0, false)
			val type = chunkHeader.copyOfRange(4, 8).toString(Charsets.UTF_8)
			val bytes = stream.readNBytes(size)

			when (type) {
				"JSON" -> json = JSONObject(bytes.toString(Charsets.UTF_8))
				"BIN" + 0.c -> buffers.add(bytes)
			}
		}

		return loadModel(fileName, json, buffers)
	}

	fun getVec4(json: JSONArray, offset: Int = 0): Vec4{
		val l = json.length() - offset
		return Vec4{ i -> if(i < l) json.getFloat(i + offset) else 1f }
	}

	fun samplerType(json: JSONObject): TextureParameters{
		return TextureParameters(
			flip = false,
			minFilter = json.getIntOrNull("minFilter") ?: GL11C.GL_LINEAR_MIPMAP_LINEAR,
			magFilter = json.getIntOrNull("magFilter") ?: GL11C.GL_LINEAR,
			wrapS = json.getIntOrNull("wrapS") ?: GL11C.GL_REPEAT,
			wrapT = json.getIntOrNull("wrapT") ?: GL11C.GL_REPEAT
		)
	}

	enum class DataType(val value: Int, val bytes: Int, val fromBytes: (ByteBuffer, Int) -> Number) {
		SIGNED_BYTE(5120, 1, { b, i -> b[i] }),
		UNSIGNED_BYTE(5121, 1, { b, i -> b[i].ub }),
		SIGNED_SHORT(5122, 2, { b, i -> b.getShort(i) }),
		UNSIGNED_SHORT(5123, 2, { b, i -> b.getUshort(i) }),
		UNSIGNED_INT(5125, 4, { b, i -> b.getInt(i).ui }),
		FLOAT(5126, 4, { b, i ->
			b.getFloat(i)
		});
	}

	enum class DataMatrix(val size: Int, val make: (ByteBuffer, Int, DataType) -> Any) {
		SCALAR(1, { b, i, d ->
			d.fromBytes(b, i)
		}),
		VEC2(2, { b, i, d ->
			when (d) {
				DataType.SIGNED_BYTE -> Vec2b(b, i)
				DataType.UNSIGNED_BYTE -> Vec2ub(b, i)
				DataType.SIGNED_SHORT -> Vec2s(b, i)
				DataType.UNSIGNED_SHORT -> Vec2us(b, i)
				DataType.UNSIGNED_INT -> Vec2ui(b, i)
				DataType.FLOAT -> Vec2(b, i)
			}
		}),
		VEC3(3, { b, i, d ->
			when (d) {
				DataType.SIGNED_BYTE -> Vec3b(b, i)
				DataType.UNSIGNED_BYTE -> Vec3ub(b, i)
				DataType.SIGNED_SHORT -> Vec3s(b, i)
				DataType.UNSIGNED_SHORT -> Vec3us(b, i)
				DataType.UNSIGNED_INT -> Vec3ui(b, i)
				DataType.FLOAT -> Vec3(b, i)
			}
		}),
		VEC4(4, { b, i, d ->
			when (d) {
				DataType.SIGNED_BYTE -> Vec4b(b, i)
				DataType.UNSIGNED_BYTE -> Vec4ub(b, i)
				DataType.SIGNED_SHORT -> Vec4s(b, i)
				DataType.UNSIGNED_SHORT -> Vec4us(b, i)
				DataType.UNSIGNED_INT -> Vec4ui(b, i)
				DataType.FLOAT -> Vec4(b, i)
			}
		}),
		MAT2(4, { b, i, d ->
			when (d) {
				DataType.FLOAT -> Mat2(b.asFloatBuffer(), i)
				else -> throw ModelParseException()
			}
		}),
		MAT3(9, { b, i, d ->
			when (d) {
				DataType.FLOAT -> Mat3(b.asFloatBuffer(), i)
				else -> throw ModelParseException()
			}
		}),
		MAT4(16, { b, i, d ->
			when (d) {
				DataType.FLOAT -> Mat4(b, i)
				else -> throw ModelParseException()
			}
		})
	}
}
