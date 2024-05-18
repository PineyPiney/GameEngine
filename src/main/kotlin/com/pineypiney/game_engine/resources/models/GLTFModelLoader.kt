package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.resources.models.animations.KeyFrame
import com.pineypiney.game_engine.resources.models.animations.MeshState
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.util.exceptions.ModelParseException
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
import kool.ByteBuffer
import kool.count
import kool.toBuffer
import org.json.JSONArray
import org.json.JSONObject
import unsigned.Ushort
import unsigned.ui
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GLTFModelLoader(val loader: ModelLoader) {

    fun loadModel(fileName: String, json: JSONObject, buffers: List<ByteBuffer>): Model{

        if(json.isEmpty) return Model.brokeModel

        val bufferViewsJson = json.getJSONArray("bufferViews")
        val bufferViews = mutableListOf<ByteBuffer>()
        bufferViewsJson.forEachObject{ o, _ ->
            val buffer = buffers[o.getInt("buffer")]
            val length = o.getInt("byteLength")
            val offset = o.getInt("byteOffset")


            bufferViews.add(buffer.slice(offset, length))
        }

        val accessorsJson = json.getJSONArray("accessors")
        val accessors = mutableListOf<Array<Any>>()
        for((_, o) in accessorsJson.objects){
            val view = bufferViews[o.getInt("bufferView")]
            val type = DataType.entries.first { it.value == o.getInt("componentType") }
            val matrix = DataMatrix.valueOf(o.getString("type"))
            val size = type.bytes * matrix.size
            val count = o.getInt("count")

            if(view.count() < size * count){
                accessors.add(arrayOf())
                continue
            }

            view.order(ByteOrder.LITTLE_ENDIAN)

            accessors.add(Array(count){ matrix.make(view, it * size, type) })
        }

        val materialsJson = json.getJSONArray("materials")
        val materials = mutableListOf<ModelMaterial>()
        materialsJson.forEachObject { materialJson, _ ->
            val name = materialJson.getString("name")
            materials.add(ModelMaterial(name, mapOf()))
        }

        val meshesJson = json.getJSONArray("meshes")
        val meshes = mutableListOf<Mesh>()
        meshesJson.forEachObject { meshJson, _ ->
            val name = meshJson.getString("name")
            val primitives = meshJson.getJSONArray("primitives")
            for((i, primitive) in primitives.objects){
                val attributes = primitive.getJSONObject("attributes")
                val pos = attributes.getInt("POSITION")
                val tex = attributes.getInt("TEXCOORD_0")
                val nor = attributes.getInt("NORMAL")

                val indices = primitive.getInt("indices")
                val material = primitive.getInt("material")

                val posArray = accessors[pos].map { it as Vec3 }
                val texArray = accessors[tex].map { it as Vec2 }
                val norArray = accessors[nor].map { it as Vec3 }
                val indArray = accessors[indices].map { (it as Number).i }

                val maxIndex = indArray.max()
                val smallArray: Pair<String, Int>? = when {
                    maxIndex >= posArray.size -> "Position" to posArray.size
                    maxIndex >= texArray.size -> "TexCoord" to texArray.size
                    maxIndex >= norArray.size -> "Normals" to norArray.size
                    else -> null
                }

                if(smallArray != null) {
                    GameEngineI.error("${smallArray.first} attribute in primitive $i of mesh $name in GLTF model $fileName is too small. It has ${smallArray.second} entries, but the index attribute requires at least $maxIndex entries")
                    continue
                }

                val vertices = Array(maxIndex + 1){ Mesh.MeshVertex(posArray[it], texArray[it], norArray[it]) }

                meshes.add(Mesh(name, vertices, indArray.toIntArray(), material = materials.getOrElse(material){ ModelMaterial.default }))
            }
        }

        // Animations

        val animations = mutableListOf<ModelAnimation>()

        val animationsJson = if(json.has("animations")) json.getJSONArray("animations") else null
        animationsJson?.forEachObject { animationJson, i ->
            val name = animationJson.getString("name")
            val states = mutableMapOf<Float, MeshState>()

            val samplers = animationJson.getJSONArray("samplers").mapObjects { samplerJson, si ->
                val times = accessors[samplerJson.getInt("input")].map { it as Number }
                val values = accessors[samplerJson.getInt("output")]
                val pairs = mutableMapOf<Float, Any>()
                times.forEachIndexed { index, number -> pairs[number.f] = values[index] }
                si to pairs.toMap()
            }.toMap()

            for((_, channelJson) in animationJson.getJSONArray("channels").objects){
                val sampler = samplers[channelJson.getInt("sampler")] ?: continue
                val target = channelJson.getJSONObject("target")
                val node = json.getJSONArray("nodes").getJSONObject(target.getInt("node"))
                val mesh = node.getString("name")
                val path = target.getString("path")

                for((t, v) in sampler){
                    val state = states.getOrPut(t){ MeshState(mesh, Vec3(), Quat(), 1f, 1) }
                    when(path){
                        "translation" -> state.translation.put(v as Vec3)
                        "rotation" -> {
                            val q = v as Vec4

                            state.rotation.put(q.x, q.y, q.z, q.w)
                        }
                    }

                }
            }

            val frames = states.map { (t, s) -> KeyFrame(t, arrayOf(s)) }

            animations.add(ModelAnimation(name, frames.toTypedArray()))
        }

        val min = Vec3(Float.POSITIVE_INFINITY)
        val max = Vec3(Float.NEGATIVE_INFINITY)
        for(mesh in meshes){
            for(vertex in mesh.vertices){
                val p = vertex.position
                if(p.x < min.x) min.x = p.x
                if(p.y < min.y) min.y = p.y
                if(p.z < min.z) min.z = p.z
                if(p.x > max.x) max.x = p.x
                if(p.y > max.y) max.y = p.y
                if(p.z > max.z) max.z = p.z
            }
        }

        val collider =
            if(min.z == max.z) Collider2D(Rect2D(Vec2(min), Vec2(max - min)))
            else Collider3D(Cuboid((min + max) * .5f, Quat.identity, max - min))
        return Model(fileName.substringAfterLast('/'), meshes.toTypedArray(), null, animations.toTypedArray(), collider)
    }

    fun loadGLTFFile(fileName: String, stream: InputStream): Model{
        val json = JSONObject(stream.readAllBytes().toString(Charsets.UTF_8))
        val buffersJson = json.getJSONArray("buffers")
        val buffers = mutableListOf<ByteBuffer>()
        for(i in 0..<buffersJson.length()){
            val bufferLocation = buffersJson.getJSONObject(i).getString("uri")
            buffers.add(loadBinFile(fileName.substringBeforeLast('/') + "/" + bufferLocation))
        }
        return loadModel(fileName, json, buffers)
    }

    fun loadBinFile(name: String): ByteBuffer{
        val stream = loader.currentStreams[name] ?: return ByteBuffer(0)
        return stream.readAllBytes().toBuffer()
    }

    // https://docs.fileformat.com/3d/glb/ Praise the lord
    fun loadGLBFile(fileName: String, stream: InputStream): Model{
        val header = stream.readNBytes(12)
        var json = JSONObject()
        val buffers = mutableListOf<ByteBuffer>()
        while(stream.available() != 0){
            val chunkHeader = stream.readNBytes(8)
            val size = chunkHeader.getInt(0, false)
            val type = chunkHeader.sliceArray(4..7).toString(Charsets.UTF_8)
            val bytes = stream.readNBytes(size)

            when(type){
                "JSON" -> json = JSONObject(bytes.toString(Charsets.UTF_8))
                "BIN" + 0.c -> buffers.add(bytes.toBuffer())
            }
        }

        return loadModel(fileName, json, buffers)
    }

    enum class DataType(val value: Int, val bytes: Int, val fromBytes: (ByteBuffer, Int) -> Number){
        SIGNED_BYTE(5120, 1, { b, i -> b[i]}),
        UNSIGNED_BYTE(5121, 1, { b, i -> b[i].ub} ),
        SIGNED_SHORT(5122, 2, { b, i -> b.getShort(i)}),
        UNSIGNED_SHORT(5123, 2, { b, i -> parseUshort(b, i)}),
        UNSIGNED_INT(5125, 4, { b, i -> b.getInt(i).ui }),
        FLOAT(5126, 4, { b, i ->
            b.getFloat(i)
        });
    }

    enum class DataMatrix(val size: Int, val make: (ByteBuffer, Int, DataType) -> Any){
        SCALAR(1, { b, i, d ->
            d.fromBytes(b, i)
         }),
        VEC2(2, { b, i, d ->
            when(d){
                DataType.SIGNED_BYTE -> Vec2b(b, i)
                DataType.UNSIGNED_BYTE -> Vec2ub(b, i)
                DataType.SIGNED_SHORT -> Vec2s(b, i)
                DataType.UNSIGNED_SHORT -> Vec2us(b, i)
                DataType.UNSIGNED_INT -> Vec2ui(b, i)
                DataType.FLOAT -> Vec2(b, i)
            }
        }),
        VEC3(3, { b, i, d ->
            when(d){
                DataType.SIGNED_BYTE -> Vec3b(b, i)
                DataType.UNSIGNED_BYTE -> Vec3ub(b, i)
                DataType.SIGNED_SHORT -> Vec3s(b, i)
                DataType.UNSIGNED_SHORT -> Vec3us(b, i)
                DataType.UNSIGNED_INT -> Vec3ui(b, i)
                DataType.FLOAT -> Vec3(b, i)
            }
        }),
        VEC4(4, { b, i, d ->
            when(d){
                DataType.SIGNED_BYTE -> Vec4b(b, i)
                DataType.UNSIGNED_BYTE -> Vec4ub(b, i)
                DataType.SIGNED_SHORT -> Vec4s(b, i)
                DataType.UNSIGNED_SHORT -> Vec4us(b, i)
                DataType.UNSIGNED_INT -> Vec4ui(b, i)
                DataType.FLOAT -> Vec4(b, i)
            }
        }),
        MAT2(4, { b, i, d ->
            when(d){
                DataType.FLOAT -> Mat2(b.asFloatBuffer(), i)
                else -> throw ModelParseException()
            }
        }),
        MAT3(9, { b, i, d ->
            when(d){
                DataType.FLOAT -> Mat3(b.asFloatBuffer(), i)
                else -> throw ModelParseException()
            }
        }),
        MAT4(16, { b, i, d ->
            when(d){
                DataType.FLOAT -> Mat4(b, i)
                else -> throw ModelParseException()
            }
        })
    }
}

fun parseUshort(b: ByteBuffer, i: Int): Ushort{
    val l = b[i + 1].ub.us shl 8
    val s = b[i].ub.i
    return l or s
}

fun JSONArray.forEachObject(predicate: (JSONObject, Int) -> Unit){
    for(i in 0..<length()) predicate(getJSONObject(i), i)
}
fun <R> JSONArray.mapObjects(predicate: (JSONObject, Int) -> R): List<R>{
    val map = mutableListOf<R>()
    for(i in (0..<length())) map.add(predicate(getJSONObject(i), i))
    return map.toList()
}

val JSONArray.objects get() = (0..<length()).associateWith { getJSONObject(it)!! }
