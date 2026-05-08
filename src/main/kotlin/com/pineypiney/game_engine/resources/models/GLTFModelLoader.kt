package com.pineypiney.game_engine.resources.models

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.models.animations.BoneState
import com.pineypiney.game_engine.resources.models.animations.KeyFrame
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PBRMaterial
import com.pineypiney.game_engine.resources.textures.Texture2D
import com.pineypiney.game_engine.resources.textures.parameters.TextureFilter
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import com.pineypiney.game_engine.resources.textures.parameters.TextureWrap
import com.pineypiney.game_engine.util.ResourceKey
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
import unsigned.ui
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLTFModelLoader {

	fun loadModel(factory: ResourceFactory, fileName: String, json: JsonObject, buffers: List<ByteArray>, map: MutableMap<ResourceKey, Model>) {

		if (json.isEmpty) return
		if(!json.has("nodes")) return

		val nodesArray = json.getAsJsonArray("nodes")

		val bufferViewsJson = json.getAsJsonArray("bufferViews")
		val bufferViews = mutableListOf<ByteArray>()
		bufferViewsJson.forEachObject { o, _ ->
			val buffer = buffers[o.getInt("buffer")]
			val length = o.getInt("byteLength")
			val offset = o.getInt("byteOffset")


			bufferViews.add(buffer.copyOfRange(offset, offset + length))
		}

		val accessorsJson = json.getAsJsonArray("accessors")
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

		val textures = loadTextures(factory, json, bufferViews)


        // Load Materials

		val materialsJson = json.getAsJsonArray("materials")
		val materials = mutableListOf<PBRMaterial>()

		materialsJson.forEachObject { materialJson, _ ->
			val name = materialJson.getString("name")
			val materialTextures = mutableMapOf<String, Texture2D>()

			fun getTextureIndex(json: JsonObject, name: String) = json.getObjectOrNull(name)?.getInt("index") ?: -1

			if(materialJson.has("pbrMetallicRoughness")) {
				val pbrJson = materialJson.getAsJsonObject("pbrMetallicRoughness")
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

		val meshesJson = json.getAsJsonArray("meshes")
		val meshCollections = mutableListOf<List<ModelMesh>>()

        meshesJson.forEachObject { meshJson, _ ->
            val name = meshJson.getString("name")
			val primitives = meshJson.getAsJsonArray("primitives")
			val meshes = mutableListOf<ModelMesh>()
            for ((_, primitive) in primitives.objects) {
				loadPrimitive(factory, fileName, name, primitive, meshes, accessors, materials)
            }
			meshCollections.add(meshes)
        }


        // Bones

        val bones = mutableSetOf<Bone>()
        if(json.has("skins")) {
			val skinJson = json.getAsJsonArray("skins")
            skinJson.forEachObject { skin, _ ->
                loadBones(fileName, nodesArray, skin, bones)
            }
        }


		// Animations

		val animations = mutableListOf<ModelAnimation>()

		val animationsJson = if (json.has("animations")) json.getAsJsonArray("animations") else null
		animationsJson?.forEachObject { animationJson, _ ->
			val name = animationJson.getString("name")
			val states = mutableMapOf<Float, MutableList<BoneState>>()

			val samplers = animationJson.getAsJsonArray("samplers").mapObjects { samplerJson, si ->
				val times = accessors[samplerJson.getInt("input")].map { it as Number }
				val values = accessors[samplerJson.getInt("output")]
				val pairs = mutableMapOf<Float, Any>()
				times.forEachIndexed { index, number -> pairs[number.f] = values[index] }
				si to pairs.toMap()
			}.toMap()

			for ((_, channelJson) in animationJson.getAsJsonArray("channels").objects) {
				val sampler = samplers[channelJson.getInt("sampler")] ?: continue
				val target = channelJson.getAsJsonObject("target")
				val node = nodesArray.getObject(target.getInt("node"))
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

		json.getAsJsonArray("scenes").forEachObject { sceneJson, _ ->
			for (nodeID in sceneJson.getAsJsonArray("nodes")) {
				if (nodeID !is JsonPrimitive) continue
				val nodeJson = nodesArray.getObject(nodeID.asInt)
				val meshID = nodeJson.getIntOrNull("mesh") ?: continue
				val meshes = meshCollections[meshID]

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
				val objectName = nodeJson.getStringOrNull("name") ?: fileName

				val root = fileName.substringUntilLast('/', "")
				map[ResourceKey(root + objectName)] = Model(objectName, meshes.toTypedArray(), bones.firstOrNull(), animations.toTypedArray(), collider)
			}
		}
	}

	fun loadPrimitive(
		factory: ResourceFactory,
		fileName: String,
		name: String,
		primitive: JsonObject,
		meshes: MutableCollection<ModelMesh>,
		accessors: List<Array<Any>>,
		materials: List<ModelMaterial>
	) {

		val attributes = primitive.getAsJsonObject("attributes")
		val attributeMap = mutableMapOf<VertexAttribute<*, *>, Array<Any>>()

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
		meshes.add(factory.createModelMesh(name, vertices, indArray.toIntArray(), material = materials.getOrElse(material) { PBRMaterial.default }))
    }

	fun loadBones(fileName: String, nodesJson: JsonArray, boneJson: JsonObject, bones: MutableSet<Bone>) {
//        val bindMatrix = boneJson.getIntOrNull("inverseBindMatrices")
		val jointIndices = boneJson.getAsJsonArray("joints").map { it.asInt }

        bones.add(loadBone(nodesJson, jointIndices.first(), jointIndices, null))
    }

	fun loadBone(nodesJson: JsonArray, boneIndex: Int, indices: List<Int>, parent: Bone?): Bone {
		val nodeJson = nodesJson.getObject(boneIndex)
        val boneName =  nodeJson.getStringOrNull("name") ?: "bone_$boneIndex"

        val translation = nodeJson.getVec3("translation") ?: Vec3()
        val rotation = nodeJson.getQuat("rotation") ?: Quat()
		// Rounding and re-normalising the quat stops tiny values from being carried over when converting between Mat4 and Quat
        val roundedRotation = Quat(Vec4 { rotation[it].round(4) }.normalize())
        val scale = nodeJson.getVec3("scale") ?: Vec3(1f)

        val bone = Bone(parent, indices.indexOf(boneIndex), boneName, boneName, (Mat4().translate(translation) * roundedRotation.toMat4()).scale(scale))

        if(nodeJson.has("children")) {
			for (f in nodeJson.getAsJsonArray("children")) {
				bone.addChild(loadBone(nodesJson, f.asInt, indices, bone))
            }
        }

        return bone
    }

	fun loadTextures(factory: ResourceFactory, json: JsonObject, bufferViews: List<ByteArray>): List<Texture2D> {
        if(!json.has("samplers")) return emptyList()
		val samplersJson = json.getAsJsonArray("samplers")
        val samplers = mutableListOf<TextureParameters>()
		for (i in 0..<samplersJson.size()) samplers.add(samplerType(samplersJson.getObject(i)))

		val imagesJson = json.getAsJsonArray("images")
        val images = mutableListOf<Triple<String, ByteArray, Boolean>>()
		for (i in 0..<imagesJson.size()) {
			val imageJson = imagesJson.getObject(i)
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

		val texturesJson = json.getAsJsonArray("textures")
		val textures = mutableListOf<Texture2D>()
		for (i in 0..<texturesJson.size()) {
			val textureJson = texturesJson.getObject(i)
            val sampler = samplers[textureJson.getInt("sampler")]
            val image = images[textureJson.getInt("source")]
			val texture = factory.loadTexture2DFromFile(image.first, image.second.inputStream(), sampler)
			textures.add(texture)
        }
        return textures
    }


	fun loadGLTFFile(resourcesLoader: ResourcesLoader, fileName: String, stream: InputStream, map: MutableMap<ResourceKey, Model>) {
		val json = JsonParser.parseString(stream.readAllBytes().toString(Charsets.UTF_8)).asJsonObject
		val buffersJson = json.getAsJsonArray("buffers")
		val buffers = mutableListOf<ByteArray>()
		for (i in 0..<buffersJson.size()) {
			val bufferLocation = buffersJson.getObject(i).getString("uri")
			buffers.add(loadBinFile(resourcesLoader, fileName.substringBeforeLast('/') + "/" + bufferLocation))
		}
		return loadModel(resourcesLoader.factory, fileName, json, buffers, map)
	}

	fun loadBinFile(resourcesLoader: ResourcesLoader, name: String): ByteArray {
		val stream = resourcesLoader.getStream(resourcesLoader.modelLocation + name) ?: return ByteArray(0)
		return stream.readAllBytes()
	}

	// https://docs.fileformat.com/3d/glb/ Praise the lord
	fun loadGLBFile(factory: ResourceFactory, fileName: String, stream: InputStream, map: MutableMap<ResourceKey, Model>) {
		stream.readNBytes(12) // Header
		var json = JsonObject()
		val buffers = mutableListOf<ByteArray>()
		while (stream.available() != 0) {
			val chunkHeader = stream.readNBytes(8) ?: break
			val size = chunkHeader.getInt(0, false)
			val type = chunkHeader.copyOfRange(4, 8).toString(Charsets.UTF_8)
			val bytes = stream.readNBytes(size)

			when (type) {
				"JSON" -> {
					val string = bytes.toString(Charsets.UTF_8)
					val parsed = JsonParser.parseString(string)
					json = parsed.asJsonObject
				}
				"BIN" + 0.c -> buffers.add(bytes)
			}
		}

		return loadModel(factory, fileName, json, buffers, map)
	}

	fun samplerType(json: JsonObject): TextureParameters {

		// GLTF uses OpenGL parameter values
		return TextureParameters(
			flip = false,
			minFilter = json.getIntOrNull("minFilter")?.let { opengl -> TextureFilter.entries.firstOrNull { it.opengl == opengl } } ?: TextureFilter.LINEAR_LINEAR,
			magFilter = json.getIntOrNull("magFilter")?.let { opengl -> TextureFilter.entries.firstOrNull { it.opengl == opengl } } ?: TextureFilter.LINEAR,
			wrapS = json.getIntOrNull("wrapS")?.let { opengl -> TextureWrap.entries.firstOrNull { it.opengl == opengl } } ?: TextureWrap.REPEAT,
			wrapT = json.getIntOrNull("wrapT")?.let { opengl -> TextureWrap.entries.firstOrNull { it.opengl == opengl } } ?: TextureWrap.REPEAT
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
