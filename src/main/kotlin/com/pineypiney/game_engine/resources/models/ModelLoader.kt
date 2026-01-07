package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.rendering.meshes.MeshVertex
import com.pineypiney.game_engine.resources.DeletableResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.models.animations.*
import com.pineypiney.game_engine.resources.models.materials.ModelMaterial
import com.pineypiney.game_engine.resources.models.materials.PhongMaterial
import com.pineypiney.game_engine.resources.models.pgm.*
import com.pineypiney.game_engine.resources.models.voxel.VoxModelLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addToCollectionOr
import com.pineypiney.game_engine.util.extension_functions.combineLists
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.transformedBy
import com.pineypiney.game_engine.util.maths.Collider2D
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.f
import glm_.i
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.math.PI

class ModelLoader private constructor() : DeletableResourcesLoader<Model>() {

	override val missing: Model = Model.brokeModel
	val modelTextures = mutableMapOf<ResourceKey, Texture>()

	val gltfLoader = GLTFModelLoader(this)
	val voxLoader = VoxModelLoader()

	fun loadModelTextures(streams: ResourcesLoader.Streams){
		streams.useEachStream { fileName, stream ->
			modelTextures[ResourceKey(fileName)] = Texture(fileName, TextureLoader.loadTextureFromStream(fileName, stream))
		}
	}

	fun loadModels(streams: ResourcesLoader.Streams) {

		streams.useEachStream { fileName, stream ->
			when (val fileType = fileName.substringAfterLast('.')) {
				"pgm" -> map[ResourceKey(fileName.removeSuffix(".$fileType"))] = loadPGMModel(fileName, stream)
				"obj" -> map[ResourceKey(fileName.removeSuffix(".$fileType"))] = loadObjModel(streams.loader, fileName, stream)
				"gltf" -> gltfLoader.loadGLTFFile(streams.loader, fileName, stream, map)
				"glb" -> gltfLoader.loadGLBFile(fileName, stream, map)
				"vox" -> map[ResourceKey(fileName.removeSuffix(".$fileType"))] = voxLoader.loadVoxModel(fileName, stream)
			}
		}
	}

	private fun loadObjModel(loader: ResourcesLoader, fileName: String, stream: InputStream): Model {
		val materials = mutableSetOf<ModelMaterial>()
		val positions = mutableListOf<Vec3>()
		val uv = mutableListOf<Vec2>()
		val normals = mutableListOf<Vec3>()
		val vertices = mutableListOf<MeshVertex>()
		val indices = mutableListOf<Int>()
		val meshes = mutableListOf<ModelMesh>()
		var name = ""
		var material: ModelMaterial = PhongMaterial.default
		for (line in stream.reader(Charsets.UTF_8).readLines()) {
			if (line.startsWith('#')) continue
			val parts = line.split(' ')
			when (parts[0]) {
				"mtllib" -> {
					materials.addAll(loadObjMaterials(loader, fileName.substringBeforeLast('/') + "/", parts[1]))
				}

				"o" -> {
					if (vertices.isNotEmpty()) {
						meshes.add(ModelMesh(name, vertices.toTypedArray(), indices.toIntArray(), material = material))
					}
					name = parts[1]
				}

				"usemtl" -> {
					material = materials.firstOrNull { it.name == parts[1] } ?: PhongMaterial.default
				}

				"v" -> positions.add(Vec3(parts[1].f, parts[2].f, parts[3].f))
				"vt" -> uv.add(Vec2(parts[1].f, parts[2].f))
				"vn" -> normals.add(Vec3(parts[1].f, parts[2].f, parts[3].f))
				"f" -> {
					when (parts.size) {
                        // Tris
						4 -> indices.addAll(listOf(vertices.size, vertices.size + 1, vertices.size + 2))
                        // Quads
						5 -> indices.addAll(
							listOf(
								vertices.size,
								vertices.size + 1,
								vertices.size + 2,
								vertices.size + 2,
								vertices.size + 3,
								vertices.size
							)
						)
					}
					for (i in 1..<parts.size) {
						val vParts = parts[i].split('/')
                        val posI = try { vParts[0].i - 1 }
                        catch (_: NumberFormatException) { 0 }
                        val texI = try { if (vParts.size >= 2 && vParts[1].isNotEmpty()) vParts[1].i - 1 else -1 }
                        catch (_: NumberFormatException) {
                            GameEngineI.warn("Failed to read tex index ${vParts[1]} in OBJ model $fileName")
                            0
                        }
                        val norI = try { if (vParts.size >= 3 && vParts[2].isNotEmpty()) vParts[2].i - 1 else -1 }
                        catch (_: NumberFormatException) {
                            GameEngineI.warn("Failed to read normal index ${vParts[2]} in OBJ model $fileName")
                            0
                        }
						vertices.add(
							MeshVertex.builder(positions[posI])
							.normal(if (norI != -1) normals[norI] else Vec3())
							.tex(if (texI != -1) uv[texI] else Vec2())
							.build()
						)
					}
				}
			}
		}
		meshes.add(ModelMesh(name, vertices.toTypedArray(), indices.toIntArray(), material = material))

		return Model(fileName, meshes.toTypedArray())
	}

	private fun loadObjMaterials(loader: ResourcesLoader, rootFile: String, fileName: String): Set<ModelMaterial> {
		val materials = mutableSetOf<ModelMaterial>()
		val stream = loader.getStream(loader.modelLocation + rootFile + fileName) ?: return materials
		var name = ""
		val textures = mutableMapOf<PhongMaterial.TextureType, Texture>()
		for (line in stream.readAllBytes().toString(Charsets.UTF_8).split('\n')) {
			if (line.isEmpty() || line.trimStart().startsWith('#')) continue
			val parts = line.trim().split(' ')
			when (parts[0]) {
				"newmtl" -> {
					if (textures.isNotEmpty()) {
						materials.add(PhongMaterial(name, textures))
					}
					name = parts[1]
				}

				"map_Ka" -> updateTextureType(textures, PhongMaterial.TextureType.AMBIENT, rootFile + parts[1])
				"map_Kd" -> updateTextureType(textures, PhongMaterial.TextureType.DIFFUSE, rootFile + parts[1])
				"map_Ks" -> updateTextureType(textures, PhongMaterial.TextureType.SPECULAR, rootFile + parts[1])
				"bump", "map_bump", "map_Kn" -> updateTextureType(textures, PhongMaterial.TextureType.NORMAL, rootFile + parts[1])

				"map_roughness" -> updateTextureType(textures, PhongMaterial.TextureType.ROUGHNESS, rootFile + parts[1])
				"map_metallic" -> updateTextureType(textures, PhongMaterial.TextureType.METALLIC, rootFile + parts[1])

			}
		}
		materials.add(PhongMaterial(name, textures))

		return materials
	}

	private fun updateTextureType(
		textures: MutableMap<PhongMaterial.TextureType, Texture>,
		type: PhongMaterial.TextureType,
		location: String
	) {
		textures[type] = modelTextures[ResourceKey(location)] ?: return
	}

	private fun loadPGMModel(fileName: String, stream: InputStream): Model {
		// https://www.hameister.org/KotlinXml.html
		val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		val doc: Document = builder.parse(stream)
		val path: XPath = xPath

		val meshes: MutableList<ModelMesh> = mutableListOf()

		val geometryMap: Map<String, Geometry> = loadGeometries(fileName, doc, path)
		val bones: Array<Bone> = loadNodes(doc, path)
		val physics: Physics = loadPhysics(doc, path)
		val animations: Array<ModelAnimation> = loadAnimations(fileName, doc, path, geometryMap.values)

		stream.close()

		for ((id, geo) in geometryMap) {
			val controller: Controller? = loadController(fileName, id, doc, path)

			// Construct Mesh Vertices
			val vertices: MutableList<MeshVertex> = mutableListOf()

			geo.vertices.indices.forEach { i ->
				val texMap: Vec2 = geo.texMaps.getOrElse(i) { Vec2() }
				val normal: Vec3 = geo.normals.getOrElse(i) { Vec3() }

				val pos: Vec3
				val weights: Array<Controller.BoneWeight> = if (controller != null) {
					pos = geo.vertices[i] transformedBy controller.matrix
					val weightsMap = controller.weights[i]
					weightsMap.map {
						val bone = bones.first { b -> b.sid == it.key }
						Controller.BoneWeight(bone.id, bone.name, it.value)
					}.toTypedArray()
				} else {
					pos = geo.vertices[i]
					arrayOf()
				}

				vertices.add(MeshVertex.builder(pos).normal(normal).tex(texMap).weights(weights).build())
			}

			meshes.add(
				ModelMesh(
					geo.name,
					vertices.toTypedArray(),
					geo.indices,
					geo.alpha,
					geo.order,
					PhongMaterial(
						"${geo.name} material",
						mapOf(PhongMaterial.TextureType.DIFFUSE to TextureLoader.findTexture(geo.texture))
					)
				)
			)
		}

		val newModel = Model(
			fileName,
			meshes.toTypedArray().reversedArray(),
			bones.getOrNull(0)?.getRoot(),
			animations,
			Collider2D(physics.collision)
		)
		return newModel
	}

	private fun loadGeometries(modelName: String, doc: Document, path: XPath = xPath): Map<String, Geometry> {

		// Get a list of the available geometries and their IDs
		val geometriesRoot = "/PGM/meshes/mesh"
		val geometries = path.evaluate(geometriesRoot, doc, XPathConstants.NODESET) as NodeList
		val geoMap: MutableMap<String, Geometry> = mutableMapOf()

		val geometryIDs = getAttributes(geometries)

		// Iterate through each geometry, using their IDs to access them specifically
		geometryIDs.forEachIndexed geo@{ geoI, id ->

			val geometryRoot = "$geometriesRoot[@id = '$id']"

			// More details
			val name: String = getFirstAttribute(doc, geometryRoot, "name", id)
			val errorMsg = "Could not load mesh $name of model $modelName"

			val texture: String = getFirstAttribute(doc, geometryRoot, "texture", "broke")
			val alpha: Float = getFirstAttribute(doc, geometryRoot, "alpha", 1f)
			val order: Int = getFirstAttribute(doc, geometryRoot, "order", geometryIDs.size - (geoI + 1))

			// Read the Sources
			val sources = DataSource.readAllDataFromXML("$geometryRoot/source", doc, path)

			// -------- Compile into triangles -------

			val verticesRoot = "$geometryRoot/triangles"

			val verticesArray = getArray(
				sources,
				errorMsg,
				"vertex",
				"$verticesRoot/input[@semantic = 'POSITION']",
				doc,
				path,
				DataSource::createVec3Array
			) ?: return@geo
			val texMapsArray = getArray(
				sources,
				errorMsg,
				"texMap",
				"$verticesRoot/input[@semantic = 'TEXCOORD']",
				doc,
				path
			) { i -> i.createVec2Array("S-T") } ?: arrayOf()
			val normalsArray = getArray(
				sources,
				errorMsg,
				"normal",
				"$verticesRoot/input[@semantic = 'NORMAL']",
				doc,
				path,
				DataSource::createVec3Array
			) ?: arrayOf()

			// Read the indices
			val indices = (path.evaluate("$verticesRoot/p", doc, XPathConstants.STRING) as String)
			val indicesArray = indices.split(" ").map { index -> index.i }.toIntArray()

			val attributes = getFirstAttributes(doc, verticesRoot)
			val count = getAttribute(attributes, "count", 0)
			val stride = getAttribute(attributes, "stride", 3)

			// There are not enough index numbers
			if (indicesArray.size < count * stride) {
				return@geo
			}

			geoMap[id] = Geometry(name, verticesArray, texMapsArray, normalsArray, indicesArray, texture, alpha, order)
		}
		return geoMap.toMap()
	}

	private fun loadNodes(doc: Document, path: XPath = xPath): Array<Bone> {

		val nodesRoot = "/PGM/bones/node"
		val nodes = path.evaluate(nodesRoot, doc, XPathConstants.NODESET) as NodeList
		val boneMap: MutableMap<String, MutableList<Bone>> = mutableMapOf()

		val nodeIDs = getAttributes(nodes)
		if (nodeIDs.isEmpty()) return arrayOf()

		nodeIDs.forEach node@{ id ->
			val nodeRoot = "$nodesRoot[@id = '$id']"
			val matrixString =
				(path.evaluate("$nodeRoot/matrix[@sid = 'transform']", doc, XPathConstants.STRING) as String)
			val floats = matrixString.split(" ").map { it.f }
			val transform = Mat4(floats).transpose()

			val idBuffer = IntArray(1) { 0 }

			val rootBone = Bone(
				null,
				0,
				getFirstAttribute(doc, nodeRoot, "name", id, path),
				getFirstAttribute(doc, nodeRoot, "sid", id, path),
				transform
			)
			boneMap[id] = mutableListOf(rootBone)
			boneMap[id]?.addAll(loadNodeChildren("$nodeRoot/node", rootBone, idBuffer, doc, path))
		}

		return boneMap[nodeIDs[0]]?.toTypedArray() ?: arrayOf()
	}

	private fun loadController(modelName: String, mesh: String, doc: Document, path: XPath = xPath): Controller? {

		val errorMsg = "Could not load the controller for mesh $mesh in model $modelName"

		val controllerRoot = "/PGM/mesh_controllers/controller[@mesh = '#$mesh']"
		if (!(path.evaluate(controllerRoot, doc, XPathConstants.BOOLEAN) as Boolean)) return null

		// Name and associated mesh of this controller
		val id: String = getFirstAttribute(doc, controllerRoot, "id", mesh, path)
		val name: String = getFirstAttribute(doc, controllerRoot, "name", mesh, path)

		// Bind Matrix is the default matrix of this mesh
		val matArray = (path.evaluate("$controllerRoot/bind_shape_matrix", doc, XPathConstants.STRING) as String)
			.split(" ")
			.map { it.f }
		val bindMatrix: Mat4 = Mat4(matArray).transpose()

		// Read the Sources
		val sources = DataSource.readAllDataFromXML("$controllerRoot/source", doc, path)

		// -------- Compile into weights -------

		val weightsRoot = "$controllerRoot/vertex_weights"

		// Get the input data as maps of strings associated with strings
		val jointSourcePair =
			getSource(errorMsg, "joints", "$weightsRoot/input[@semantic = 'JOINT']", doc, path) ?: return null
		val weightSourcePair =
			getSource(errorMsg, "weights", "$weightsRoot/input[@semantic = 'WEIGHT']", doc, path) ?: return null

		// Joints is the bones that affect this mesh,
		// and weights is the relative weight of the bones in joints
		val joints = sources.firstOrNull { it pointedBy jointSourcePair } ?: DataSource.EMPTY
		val weights = sources.firstOrNull { it pointedBy weightSourcePair } ?: DataSource.EMPTY

		// vCounts is the number of bones that affect each vertex
		val vCounts = (path.evaluate("$weightsRoot/vcount", doc, XPathConstants.STRING) as String)
			.trim()
			.split(" ")
			.map { it.i }

		// v is the index of the affecting bone and the index of the associated weight in joints and weights DataSources
		val v = (path.evaluate("$weightsRoot/v", doc, XPathConstants.STRING) as String)
			.trim()
			.split(" ")
			.chunked(2)
			.map { Vec2i(it[0].i, it[1].i) }

		var i = 0
		val boneWeights: MutableList<Map<String, Float>> = mutableListOf()
		for (count in vCounts) {
			val map: MutableMap<String, Float> = mutableMapOf()
			repeat(count) {
				map[joints["JOINT"][v[i].x]] = weights["WEIGHT"][v[i].y].f
				i++
			}
			boneWeights.add(map.toMap())
		}

		return Controller(id, name, mesh, bindMatrix, boneWeights)

	}

	private fun loadPhysics(doc: Document, path: XPath = xPath): Physics {
		val physicsRoot = "/PGM/physics"
		val colliderRoot = "$physicsRoot/collider"
		val collider = if (path.evaluate(colliderRoot, doc, XPathConstants.BOOLEAN) as Boolean) {
			val originString = path.evaluate("$colliderRoot/origin", doc, XPathConstants.STRING) as String
			val origin = Vec2(originString.split(" ").map { s -> s.f })
			val sizeString = path.evaluate("$colliderRoot/size", doc, XPathConstants.STRING) as String
			val size = Vec2(sizeString.split(" ").map { s -> s.f })
			Rect2D(origin, size)
		} else Rect2D(Vec2(), Vec2(1))

		return Physics(collider)
	}

	private fun loadAnimations(
		modelName: String,
		doc: Document,
		path: XPath = xPath,
		meshes: Iterable<Geometry>
	): Array<ModelAnimation> {

		val animationRoot = "/PGM/animations/animation"
		val animationMap: MutableList<ModelAnimation> = mutableListOf()

		val animations = path.evaluate(animationRoot, doc, XPathConstants.NODESET) as NodeList
		val animationsIDs = getAttributes(animations)

		for (id in animationsIDs) {

			val animRoot = "$animationRoot[@id = '$id']/animation"

			val bones = path.evaluate(animRoot, doc, XPathConstants.NODESET) as NodeList
			val boneIDs: MutableList<String> = mutableListOf()
			val meshIDs: MutableList<String> = mutableListOf()
			for (x in 0 until bones.length) {

				val type = getAttribute(bones.item(x).attributes, "name", "")
				val value = getAttribute(bones.item(x).attributes, "id", "")
				when (type) {
					"Armature" -> boneIDs.add(value)
					"Mesh" -> meshIDs.add(value)
				}
			}

			val animMeshes = meshes.filter { mesh -> meshIDs.any { it.removePrefix("Animation_") == mesh.name } }
			val bStates = loadBoneStates(modelName, id, boneIDs.toTypedArray(), animRoot, doc, path)
			val mStates = loadMeshStates(modelName, id, meshIDs.toTypedArray(), animRoot, animMeshes, doc, path)
			if (bStates.isEmpty() && mStates.isEmpty()) {
				GameEngineI.warn("There were no states loaded for animation $id for model $modelName")
				continue
			}

			val stateMap = bStates.combineLists(mStates)
			val frames: Array<KeyFrame> =
				stateMap.map { (time, states) -> KeyFrame(time, states) }.sorted().toTypedArray()
			animationMap.add(ModelAnimation(id, frames))
		}
		return animationMap.toTypedArray()
	}

	private fun loadNodeChildren(
		root: String,
		parent: Bone,
		idBuffer: IntArray,
		doc: Document,
		path: XPath = xPath
	): Collection<Bone> {

		val nodes = path.evaluate(root, doc, XPathConstants.NODESET) as NodeList
		val boneList: MutableList<Bone> = mutableListOf()

		val nodeIDs = getAttributes(nodes)

		for (id in nodeIDs) {
			val nodeRoot = "$root[@id = '$id']"
			val matrixString =
				(path.evaluate("$nodeRoot/matrix[@sid = 'transform']", doc, XPathConstants.STRING) as String)
			val floats = matrixString.split(" ").map { it.f }
			val transform = Mat4(floats).transpose()

			idBuffer[0]++
			val child = Bone(
				parent,
				idBuffer[0],
				getFirstAttribute(doc, nodeRoot, "name", id, path),
				getFirstAttribute(doc, nodeRoot, "sid", id, path),
				transform
			)
			parent.addChild(child)

			boneList.add(child)
			boneList.addAll(loadNodeChildren("$nodeRoot/node", child, idBuffer, doc, path))
		}

		return boneList
	}

	private fun loadBoneStates(
		modelName: String,
		animName: String,
		boneIDs: Array<String>,
		animRoot: String,
		doc: Document,
		path: XPath
	): MutableMap<Float, MutableList<State>> {

		val stateMap: MutableMap<Float, MutableList<State>> = mutableMapOf()

		for (bone in boneIDs) {
			val errorMsg = "Could not load the animation of bone $bone in animation $animName in model $modelName"

			// Read the Sources
			val meshRoot = "$animRoot[@id = '$bone']"
			val sources = DataSource.readAllDataFromXML("$meshRoot/source", doc, path)

			// -------- Compile into Animation -------

			// Get the input data as maps of strings associated with strings
			val inputRoot = "$meshRoot/sampler/input"

			val timeSourcePair = getSource(errorMsg, "time", "$inputRoot[@semantic = 'TIME']", doc, path)
			val tranSourcePair = getSource(errorMsg, "translation", "$inputRoot[@semantic = 'TRANSLATION']", doc, path)
			val rotSourcePair = getSource(errorMsg, "rotation", "$inputRoot[@semantic = 'ROTATION']", doc, path)

			val times = sources.firstOrNull { it pointedBy timeSourcePair } ?: continue
			val translations = sources.firstOrNull { it pointedBy tranSourcePair } ?: DataSource.EMPTY
			val rotations = sources.firstOrNull { it pointedBy rotSourcePair } ?: DataSource.EMPTY

			val timesArray = times.createFloatArray("TIME")
			val tranArray = translations.createVec3Array()
			val rotArray = rotations.createVec3Array("x-y-ROTATION")

			for (i in timesArray.indices) {
				val time = timesArray[i]
				val translation = if (tranArray.size > i) tranArray[i] else Vec3()
				val rotation = if (rotArray.size > i) rotArray[i] else Vec3()

				// Initialise list if it doesn't yet exist, and don't forget to convert degrees to radians
				stateMap.addToCollectionOr(
					time,
					BoneState(bone.removePrefix("Animation_"), translation, Quat(rotation * -PI.f / 180f)),
				) { mutableListOf() }
			}
		}

		return stateMap
	}

	private fun loadMeshStates(
		modelName: String,
		animName: String,
		meshIDs: Array<String>,
		animRoot: String,
		meshes: Iterable<Geometry>,
		doc: Document,
		path: XPath
	): MutableMap<Float, MutableList<State>> {
		val stateMap: MutableMap<Float, MutableList<State>> = mutableMapOf()

		for (mesh in meshIDs) {
			val errorMsg = "Could not load the animation of mesh $mesh in animation $animName in model $modelName"

			// Read the Sources
			val meshRoot = "$animRoot[@id = '$mesh']"
			val sources = DataSource.readAllDataFromXML("$meshRoot/source", doc, path)

			// -------- Compile into Animation -------

			// Get the input data as maps of strings associated with strings
			val inputRoot = "$meshRoot/sampler/input"

			val timeSourcePair = getSource(errorMsg, "time", "$inputRoot[@semantic = 'TIME']", doc, path)
			val tranSourcePair = getSource(errorMsg, "translation", "$inputRoot[@semantic = 'TRANSLATION']", doc, path)
			val rotSourcePair = getSource(errorMsg, "rotation", "$inputRoot[@semantic = 'ROTATION']", doc, path)
			val alphaSourcePair = getSource(errorMsg, "alpha", "$inputRoot[@semantic = 'ALPHA']", doc, path)
			val orderSourcePair = getSource(errorMsg, "order", "$inputRoot[@semantic = 'ORDER']", doc, path)

			val times = sources.firstOrNull { it pointedBy timeSourcePair } ?: DataSource.EMPTY
			val translations = sources.firstOrNull { it pointedBy tranSourcePair } ?: DataSource.EMPTY
			val rotations = sources.firstOrNull { it pointedBy rotSourcePair } ?: DataSource.EMPTY
			val alphas = sources.firstOrNull { it pointedBy alphaSourcePair } ?: DataSource.EMPTY
			val orders = sources.firstOrNull { it pointedBy orderSourcePair } ?: DataSource.EMPTY
			// -------- Compile into Animation -------

			val timesArray = times.createFloatArray("TIME")
			val tranArray = translations.createVec3Array()
			val rotArray = rotations.createVec3Array("x-y-ROTATION")
			val alphaArray = alphas.createFloatArray("ALPHA")
			val orderArray = orders.createIntArray("ORDER")

			val geo = meshes.firstOrNull { it.name == mesh.removePrefix("Animation_") }

			for (i in timesArray.indices) {
				val time = timesArray[i]
				val translation = if (tranArray.size > i) tranArray[i] else Vec3()
				val rotation = if (rotArray.size > i) rotArray[i] else Vec3()
				val alpha = if (alphaArray.size > i) alphaArray[i] else geo?.alpha ?: 1f
				val order = if (orderArray.size > i) orderArray[i] else geo?.order ?: 0

				// Initialise list if it doesn't yet exist, and don't forget to convert degrees to radians
				stateMap.addToCollectionOr(
					time,
					MeshState(
						mesh.removePrefix("Animation_"),
						translation,
						Quat(rotation * -PI.f / 180f).inverse(),
						alpha,
						order
					),
				) { mutableListOf() }
			}
		}

		return stateMap
	}

	override fun delete() {
		super.delete()
		modelTextures.delete()
	}

	companion object {
		val INSTANCE: ModelLoader = ModelLoader()

		val xPath: XPath = XPathFactory.newInstance().newXPath()

		fun getModel(key: ResourceKey) = INSTANCE[key]
		operator fun get(key: ResourceKey) = INSTANCE[key]
	}
}