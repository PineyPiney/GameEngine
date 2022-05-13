package com.pineypiney.game_engine.visual.game_objects

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.visual.IScreenObject
import com.pineypiney.game_engine.visual.Renderable
import com.pineypiney.game_engine.visual.ScreenObjectCollection
import com.pineypiney.game_engine.visual.Storable
import com.pineypiney.game_engine.visual.util.Transform
import com.pineypiney.game_engine.visual.util.collision.CollisionBox
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class GameObject : IScreenObject, Renderable, Storable, Copyable<GameObject> {

    abstract val id: ResourceKey

    open val shader: Shader = defaultShader
    abstract val collision: CollisionBox

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

    override fun render(vp: Mat4, tickDelta: Double) {}

    override fun updateAspectRatio(window: Window) {}

    fun move(movement: Vec2): Vec2 {
        val collidedMove = movement.copy()

        // Create a temporary collision box in the new position to calculate collisions
        val newCollision = this.collision.copy()
        newCollision.origin += (movement / this.scale)

        // Iterate over all collision boxes sharing object collections and
        // eject this collision boxes object if the collision boxes collide
        objects.forEach {
            it.forEachCollision { box ->
                if(box != this.collision) collidedMove plusAssign  newCollision.getEjectionVector(box)
            }
        }

        // If a collision is detected in either direction then set the velocity to 0
        if(collidedMove.x != movement.x) velocity.x = 0f
        if(collidedMove.y != movement.y) velocity.y = 0f

        return collidedMove
    }

    fun getWidth(): Float{
        return scale.x
    }

    fun translate(move: Vec2){
        transform.translate(move)
    }

    fun setPosition(pos: Vec3){
        position = Vec2(pos)
        depth = pos.z.i
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