package com.pineypiney.game_engine.visual.util.collision

import com.pineypiney.game_engine.visual.game_objects.GameObject
import com.pineypiney.game_engine.visual.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.absMinOf
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL46C
import kotlin.math.abs

abstract class CollisionBox(var parent: GameObject?, val origin: Vec2, val size: Vec2): Copyable<CollisionBox> {

    var active: Boolean = true

    val relModel = I.translate(Vec3(origin)).scale(Vec3(size))

    val worldOrigin: Vec2; get()  = (parent?.position ?: Vec2()) + ((parent?.scale ?: Vec2()) * this.origin)

    val worldScale: Vec2; get() = ((parent?.scale ?: Vec2()) * this.size)

    val left; get() = if(worldScale.x > 0) this.worldOrigin.x else this.worldOrigin.x + this.worldScale.x
    val right; get() = left + abs(this.worldScale.x)
    val bottom; get() = if(worldScale.y > 0) this.worldOrigin.y else this.worldOrigin.y + this.worldScale.y
    val top; get() = bottom + abs(this.worldScale.y)
    val width; get() = worldScale.x
    val height; get() = worldScale.y

    fun render(vp: Mat4){

        Shape.cornerSquareShape.bind()
        val finalModel = (parent?.transform?.model ?: I) * this.relModel

        val colliderShader = CollisionBox.colliderShader
        colliderShader.use()
        colliderShader.setMat4("vp", vp)
        colliderShader.setMat4("model", finalModel)
        colliderShader.setVec4("colour", Vec4(1))

        GL46C.glDrawArrays(GL46C.GL_TRIANGLES, 0, 6)
    }

    infix fun collidesWith(other: CollisionBox): Boolean{
        return this.parent != other.parent && this.active && other.active &&
                !(this.left > other.right || other.left > this.right || this.bottom > other.top || other.bottom > this.top)
    }

    fun isColliding(collisions: Collection<CollisionBox> = parent?.objects?.flatMap { it.getAllCollisions() } ?: emptySet()): Boolean{
        for(c in collisions.toSet()) if(this collidesWith c) return true
        return false
    }

    fun getEjectionVector(other: CollisionBox): Vec2{
        if(!(this collidesWith other)) return Vec2()

        val eLeft = other.left - this.right
        val eRight = other.right - this.left
        val eDown = other.bottom - this.top
        val eUp = other.top - this.bottom

        val smallest = absMinOf(eLeft, eRight, eDown, eUp)

        return if(smallest == eLeft || smallest == eRight) Vec2(smallest, 0)
            else Vec2(0, smallest)
    }

    companion object{
        val colliderShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))
    }
}