package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.IScreenObject
import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.util.Transform
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.ResourceKey
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class GameObject : IScreenObject, Renderable, Storable, Copyable<GameObject> {

    abstract val id: ResourceKey

    open val shader: Shader = defaultShader

    override var visible: Boolean = true

    override val objects: MutableList<ScreenObjectCollection> = mutableListOf()

    var transform: Transform = Transform.origin.c

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

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {}

    override fun updateAspectRatio(window: Window) {}

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

    override fun addTo(objects: ScreenObjectCollection){
        objects.gameItems.add(this)
    }

    override fun removeFrom(objects: ScreenObjectCollection) {
        objects.gameItems.remove(this)
    }

    abstract fun toData(): Array<String>

    override fun delete() {
        shader.delete()
    }

    companion object{
        val defaultObject: GameObject; get() = TexturedGameObject(ResourceKey("broke"), Texture.brokeTexture)
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex\\2D"), ResourceKey("fragment\\texture"))
    }
}