package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.util.Transform
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.ResourceKey
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class GameObject : Initialisable, Storable, Copyable<GameObject> {

    abstract val id: ResourceKey

    open val shader: Shader = defaultShader

    override val objects: MutableList<ObjectCollection> = mutableListOf()

    var transform: Transform = Transform.origin

    open var velocity: Vec2 = Vec2()

    var position: Vec2
        get() = transform.position
        set(value){
            transform.position = value
        }
    open var scale: Vec2
        get() = transform.scale
        set(value){
            transform.scale = value
        }
    open var rotation: Float
        get() = transform.rotation
        set(value){
            transform.rotation = value
        }

    // Items are rendered in order of depth, from inf to -inf
    open var depth: Int = 0

    override fun init() {}

    fun getWidth(): Float{
        return scale.x
    }

    fun setPosition(pos: Vec3){
        position = Vec2(pos)
        depth = pos.z.i
    }

    fun translate(move: Vec2){
        transform.translate(move)
    }

    fun rotate(angle: Float){
        transform.rotate(angle)
    }

    fun scale(mult: Vec2){
        transform.scale(mult)
    }

    override fun addTo(objects: ObjectCollection){
        objects.gameItems.add(this)
    }

    override fun removeFrom(objects: ObjectCollection) {
        objects.gameItems.remove(this)
    }

    override fun delete() {
        for(o in objects) { o.gameItems.remove(this) }
    }

    companion object{
        val defaultObject: GameObject; get() = TexturedGameObject(ResourceKey("broke"), Texture.brokeTexture)
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex\\2D"), ResourceKey("fragment\\texture"))
    }
}