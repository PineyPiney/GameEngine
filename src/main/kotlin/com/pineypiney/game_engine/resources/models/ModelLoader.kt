package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.objects.util.collision.SoftCollisionBox
import com.pineypiney.game_engine.resources.DeletableResourcesLoader
import com.pineypiney.game_engine.resources.models.animations.*
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addToListOr
import com.pineypiney.game_engine.util.extension_functions.combineLists
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.s
import glm_.f
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.InputStream
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.PI

class ModelLoader private constructor(): DeletableResourcesLoader<Model>() {

    override val missing: Model = Model.brokeModel

    fun loadModels(streams: Map<String, InputStream>) {

        streams.filter { it.key.endsWith(".pgm") }.forEach { (fileName, stream) ->

            // https://www.hameister.org/KotlinXml.html
            val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc: Document = builder.parse(stream)
            val path: XPath = xPath

            val meshes: MutableList<Mesh> = mutableListOf()

            val geometryMap: Map<String, Geometry> = loadGeometries(fileName, doc, path)
            val bones: Array<Bone> = loadNodes(doc, path)
            val physics: Physics = loadPhysics(doc, path)
            val animations: Array<Animation> = loadAnimations(fileName, doc, path, geometryMap.values)

            stream.close()

            for((id, geo) in geometryMap){
                val controller: Controller? = loadController(fileName, id, doc, path)

                // Construct Mesh Vertices
                val vertices: MutableList<Mesh.MeshVertex> = mutableListOf()

                geo.vertices.indices.forEach { i ->
                    val texMap: Vec2 = geo.texMaps[i]

                    val pos: VertexPosition
                    val weights: Array<Controller.BoneWeight> = if(controller != null){
                        pos = geo.vertices[i].transform(controller.matrix)
                        val weightsMap = controller.weights[pos.id]
                        weightsMap.map {
                            val bone = bones.first { b -> b.sid == it.key }
                            Controller.BoneWeight(bone.id, bone.name,  it.value)
                        }.toTypedArray()
                    }
                    else {
                        pos = geo.vertices[i]
                        arrayOf()
                    }

                    vertices.add(Mesh.MeshVertex(pos, texMap, weights))
                }

                meshes.add(Mesh(geo.name, vertices.toTypedArray(), geo.indices, geo.texture, geo.alpha, geo.order))
            }

            val newModel = Model(meshes.toTypedArray().reversedArray(), bones.getOrNull(0)?.getRoot(), animations, fileName)
            newModel.collisionBox = physics.collision
            map[ResourceKey(fileName.removeSuffix(".pgm"))] = newModel
        }
    }

    private fun loadGeometries(modelName: String, doc: Document, path: XPath = xPath): Map<String, Geometry>{

        // Get a list of the available geometries and their IDs
        val geometriesRoot = "/PGM/meshes/mesh"
        val geometries = path.evaluate(geometriesRoot, doc, XPathConstants.NODESET) as NodeList
        val geoMap: MutableMap<String, Geometry> = mutableMapOf()

        val geometryIDs = getAttributes(geometries)

        // Iterate through each geometry, using their IDs to access them specifically
        geometryIDs.forEachIndexed geo@ { geoI, id ->

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

            val verticesSourcePair = getSource(errorMsg, "vertex", "$verticesRoot/input[@semantic = 'POSITION']", doc, path) ?: return@geo
            val texMapSourcePair = getSource(errorMsg, "texMap", "$verticesRoot/input[@semantic = 'TEXCOORD']", doc, path) ?: return@geo

            val verticesSource = sources.firstOrNull { it pointedBy verticesSourcePair }
            val texMapSource = sources.firstOrNull { it pointedBy texMapSourcePair }

            val points = verticesSource?.createVec2Array() ?: arrayOf()
            val verticesArray: Array<VertexPosition> = points.mapIndexed { i, v -> VertexPosition(i, v) }.toTypedArray()
            val texMapsArray: Array<Vec2> = texMapSource?.createVec2Array("S-T") ?: arrayOf()

            // Read the indices
            val indices = (path.evaluate("$verticesRoot/p", doc, XPathConstants.STRING) as String)
            val indicesArray = indices.split(" ").map { index -> index.i }.toIntArray()

            val attributes = getFirstAttributes(doc, verticesRoot)
            val count = getAttribute(attributes , "count", 0)
            val stride = getAttribute(attributes, "stride", 3)

            // There are not enough index numbers
            if(indicesArray.size < count * stride){
                return@geo
            }

            geoMap[id] = Geometry(name, verticesArray, texMapsArray, indicesArray, texture, alpha, order)
        }
        return geoMap.toMap()
    }

    private fun loadNodes(doc: Document, path: XPath = xPath): Array<Bone>{

        val nodesRoot = "/PGM/bones/node"
        val nodes = path.evaluate(nodesRoot, doc, XPathConstants.NODESET) as NodeList
        val boneMap: MutableMap<String, MutableList<Bone>> = mutableMapOf()

        val nodeIDs = getAttributes(nodes)
        if(nodeIDs.isEmpty()) return arrayOf()

        nodeIDs.forEach node@ { id ->
            val nodeRoot = "$nodesRoot[@id = '$id']"
            val matrixString = (path.evaluate("$nodeRoot/matrix[@sid = 'transform']", doc, XPathConstants.STRING) as String)
            val floats = matrixString.split(" ").map { it.f }
            val transform = Mat4(floats).transpose()

            val idBuffer = IntArray(1) {0}

            val rootBone = Bone(null, 0, getFirstAttribute(doc, nodeRoot, "name", id, path), getFirstAttribute(doc, nodeRoot, "sid", id, path), transform)
            boneMap[id] = mutableListOf(rootBone)
            boneMap[id]?.addAll(loadNodeChildren("$nodeRoot/node", rootBone, idBuffer, doc, path))
        }

        return boneMap[nodeIDs[0]]?.toTypedArray() ?: arrayOf()
    }

    private fun loadController(modelName: String, mesh: String, doc: Document, path: XPath = xPath): Controller?{

        val errorMsg = "Could not load the controller for mesh $mesh in model $modelName"

        val controllerRoot = "/PGM/mesh_controllers/controller[@mesh = '#$mesh']"
        if(!(path.evaluate(controllerRoot, doc, XPathConstants.BOOLEAN) as Boolean)) return null

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
        val jointSourcePair = getSource(errorMsg, "joints", "$weightsRoot/input[@semantic = 'JOINT']", doc, path) ?: return null
        val weightSourcePair = getSource(errorMsg, "weights", "$weightsRoot/input[@semantic = 'WEIGHT']", doc, path) ?: return null

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
        for(count in vCounts){
            val map: MutableMap<String, Float> = mutableMapOf()
            for(index in 0 until count){
                map[joints["JOINT"][v[i].x]] = weights["WEIGHT"][v[i].y].f
                i++
            }
            boneWeights.add(map.toMap())
        }

        return Controller(id, name, mesh, bindMatrix, boneWeights)

    }

    private fun loadPhysics(doc: Document, path: XPath = xPath): Physics{
        val physicsRoot = "/PGM/physics"
        val colliderRoot = "$physicsRoot/collider"
        val collider = if(path.evaluate(colliderRoot, doc, XPathConstants.BOOLEAN) as Boolean){
            val originString = path.evaluate("$colliderRoot/origin", doc, XPathConstants.STRING) as String
            val origin = Vec2(originString.split(" ").map { s -> s.f })
            val sizeString = path.evaluate("$colliderRoot/size", doc, XPathConstants.STRING) as String
            val size = Vec2(sizeString.split(" ").map { s -> s.f})
            SoftCollisionBox(null, origin, size)
        }
        else SoftCollisionBox(null, Vec2(), Vec2(1))

        return Physics(collider)
    }

    private fun loadAnimations(modelName: String, doc: Document, path: XPath = xPath, meshes: Iterable<Geometry>): Array<Animation>{

        val animationRoot = "/PGM/animations/animation"
        val animationMap: MutableList<Animation> = mutableListOf()

        val animations = path.evaluate(animationRoot, doc, XPathConstants.NODESET) as NodeList
        val animationsIDs = getAttributes(animations)

        for(id in animationsIDs){

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
            if(bStates.isEmpty() && mStates.isEmpty()){
                GameEngine.logger.warn("There were no states loaded for animation $id for model $modelName")
                continue
            }

            val stateMap = bStates.combineLists(mStates)
            val frames: Array<KeyFrame> = stateMap.map { (time, states) -> KeyFrame(time, states) }.sorted().toTypedArray()
            animationMap.add(Animation(id, frames))
        }
        return animationMap.toTypedArray()
    }

    private fun loadNodeChildren(root: String, parent: Bone, idBuffer: IntArray, doc: Document, path: XPath = xPath): Collection<Bone>{

        val nodes = path.evaluate(root, doc, XPathConstants.NODESET) as NodeList
        val boneList: MutableList<Bone> = mutableListOf()

        val nodeIDs = getAttributes(nodes)

        for(id in nodeIDs){
            val nodeRoot = "$root[@id = '$id']"
            val matrixString = (path.evaluate("$nodeRoot/matrix[@sid = 'transform']", doc, XPathConstants.STRING) as String)
            val floats = matrixString.split(" ").map { it.f }
            val transform = Mat4(floats).transpose()

            idBuffer[0]++
            val child = Bone(parent, idBuffer[0], getFirstAttribute(doc, nodeRoot, "name", id, path), getFirstAttribute(doc, nodeRoot, "sid", id, path), transform)
            parent.addChild(child)

            boneList.add(child)
            boneList.addAll(loadNodeChildren("$nodeRoot/node", child, idBuffer, doc, path))
        }

        return boneList
    }
    private fun loadBoneStates(modelName: String, animName: String, boneIDs: Array<String>, animRoot: String, doc: Document, path: XPath): MutableMap<Float, MutableList<State>>{

        val stateMap: MutableMap<Float, MutableList<State>> = mutableMapOf()

        for(bone in boneIDs){
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
            val tranArray = translations.createVec2Array()
            val rotArray = rotations.createFloatArray("ROTATION")

            for(i in timesArray.indices){
                val time = timesArray[i]
                val translation = if(tranArray.size > i) tranArray[i] else Vec2()
                val rotation = if(rotArray.size > i) rotArray[i] else 0f

                // Initialise list if it doesn't yet exist, and don't forget to convert degrees to radians
                stateMap.addToListOr(time,
                    BoneState(bone.removePrefix("Animation_"), translation, rotation * -PI.f / 180f),
                ){ mutableListOf() }
            }
        }

        return stateMap
    }
    private fun loadMeshStates(modelName: String, animName: String, meshIDs: Array<String>, animRoot: String, meshes: Iterable<Geometry>, doc: Document, path: XPath): MutableMap<Float, MutableList<State>>{
        val stateMap: MutableMap<Float, MutableList<State>> = mutableMapOf()

        for(mesh in meshIDs){
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
            val tranArray = translations.createVec2Array()
            val rotArray = rotations.createFloatArray("ROTATION")
            val alphaArray = alphas.createFloatArray("ALPHA")
            val orderArray = orders.createIntArray("ORDER")

            val geo = meshes.firstOrNull { it.name == mesh.removePrefix("Animation_") }

            for(i in timesArray.indices){
                val time = timesArray[i]
                val translation = if(tranArray.size > i) tranArray[i] else Vec2()
                val rotation = if(rotArray.size > i) rotArray[i] else 0f
                val alpha = if(alphaArray.size > i) alphaArray[i] else geo?.alpha ?: 1f
                val order = if(orderArray.size > i) orderArray[i] else geo?.order ?: 0

                // Initialise list if it doesn't yet exist, and don't forget to convert degrees to radians
                stateMap.addToListOr(time,
                    MeshState(mesh.removePrefix("Animation_"), translation, rotation * -PI.f / 180f, alpha, order),
                ){ mutableListOf() }
            }
        }

        return stateMap
    }

    companion object{
        val INSTANCE: ModelLoader = ModelLoader()

        val xPath: XPath = XPathFactory.newInstance().newXPath()

        fun getModel(key: ResourceKey) = INSTANCE[key]
        operator fun get(key: ResourceKey) = INSTANCE[key]

        fun loadMaterial(stream: InputStream, materialTextures: Array<Texture>): ModelMaterial?{
            val scanner = Scanner(stream)
            var name: String? = null
            var baseColour = Vec3(1)
            val textures: MutableMap<String, Texture> = mutableMapOf()
            while(scanner.hasNextLine()){
                val ln = scanner.nextLine()

                val split = ln.split(" ")
                when(split[0]){
                    "newmtl" -> name = split[1]
                    "Kd" -> baseColour = Vec3(split[1].f, split[2].f, split[3].f)
                    "map_Kd" -> {
                        textures[split[0]] = try{
                            materialTextures.toList().first { it.fileName.substringAfterLast(s) == (split[1]) }
                        }
                        catch (e: NoSuchElementException){
                            Texture.broke
                        }
                    }
                }
            }

            if(name != null){
                return ModelMaterial(name, textures, baseColour)
            }
            else{
                GameEngine.logger.warn("This is not a valid material file")
            }
            return null
        }
    }

    data class VertexPosition(val id: Int, var pos: Vec2): Copyable<VertexPosition> {

        fun transform(m: Mat4): VertexPosition{
            return VertexPosition(this.id, Vec2(m * Vec4(this.pos)))
        }

        override fun copy(): VertexPosition{
            return VertexPosition(id, pos.copy())
        }

        override fun toString(): String {
            return "$pos[$id]"
        }
    }
}