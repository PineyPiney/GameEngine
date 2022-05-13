package com.pineypiney.game_engine.resources.models.new_model

import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.resources.models.Model
//import com.pineypiney.pixelgameimporter.model.ModelImporter

class NewModelLoader private constructor(): AbstractResourceLoader<Model>() {

    /*
    private val modelMap: MutableMap<ResourceKey, Model> = mutableMapOf()

    fun loadModels(streams: Map<String, InputStream>) {

        streams.filter { it.key.endsWith(".pgm") }.forEach { (fileName, stream) ->

            val model = ModelImporter.loadModel(stream)

            val meshes: Array<Mesh> = compileMeshes(model)
            val bone: Bone? = compileBones(model)
            val physics: Physics = compilePhysics(model)
            val animations: Array<Animation> = compileAnimations(model)

            val newModel = Model(meshes, bone, animations)
            newModel.collisionBox = physics.collision
            modelMap[ResourceKey(fileName.removePrefix("models/").removeSuffix(".pgm"))] = newModel
        }
    }

    private fun compileMeshes(model: com.pineypiney.pixelgameimporter.model.Model): Array<Mesh>{
        val meshes = mutableListOf<Mesh>()

        for (mesh in model.meshes) {
            val vertices = mutableListOf<Mesh.MeshVertex>()
            for(i in mesh.ids.indices){
                vertices.add(
                    Mesh.MeshVertex(
                        ModelLoader.VertexPosition(mesh.ids[i], Vec3(mesh.poses[i])),
                        mesh.normals[i],
                        mesh.texCoords[i],
                        mesh.weights[i].map { Controller.BoneWeight(it.key, " ", it.value) }.toTypedArray()
                    )
                )
            }
            meshes.add(Mesh(vertices.toTypedArray()))
        }

        return meshes.toTypedArray()
    }
    private fun compileBones(model: com.pineypiney.pixelgameimporter.model.Model): Bone?{

        model.bone?.let { bone ->
            val rootBone = Bone(null, bone.id, bone.name, bone.sid, bone.parentTransform)
            compileBoneChildren(rootBone, bone)
        }
        return null
    }
    private fun compilePhysics(model: com.pineypiney.pixelgameimporter.model.Model): Physics {
        val physics = model.physics

        return Physics(SoftCollisionBox(null, physics.collisionOrigin, physics.collisionSize))
    }
    private fun compileAnimations(model: com.pineypiney.pixelgameimporter.model.Model): Array<Animation>{
        return model.animations.map { animation ->
            Animation(animation.name, animation.frames.map { frame ->
                KeyFrame(frame.key, frame.value.map { state ->
                    BoneState(state.boneName, state.angle, state.translation)
                }.toTypedArray())
            }.toTypedArray())
        }.toTypedArray()
    }

    private fun compileBoneChildren(parent: Bone, source: com.pineypiney.pixelgameimporter.model.Bone){

        source.children.forEach child@ { child ->
            val newChild = Bone(parent, child.id, child.name, child.sid, child.parentTransform)
            compileBoneChildren(newChild, child)
            parent.addChild(newChild)
        }
    }

    fun getModel(key: ResourceKey): Model {
        return modelMap[key] ?: Model.brokeModel
    }

    companion object{
        val INSTANCE: NewModelLoader = NewModelLoader()

        fun getModel(key: ResourceKey) = INSTANCE.getModel(key)

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
                            Texture.brokeTexture
                        }
                    }
                }
            }

            if(name != null){
                return ModelMaterial(name, textures, baseColour)
            }
            else{
                System.err.println("This is not a valid material file")
            }
            return null
        }
    }
     */

    override fun delete() {
        TODO("Not yet implemented")
    }
}