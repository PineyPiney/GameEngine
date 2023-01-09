package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.extension_functions.fromMat4Translation
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class CollisionBox2D(var parent: GameObject2D?, val box: Rect2D): Copyable<CollisionBox2D> {

    constructor(parent: GameObject2D?, origin: Vec2, size: Vec2): this(parent, Rect2D(origin, size))

    var active: Boolean = true

    val origin: Vec2 get() = box.origin
    val size: Vec2 get() = box.size
    val relModel = I.translate(Vec3(origin)).scale(Vec3(size)).rotate(box.angle, normal)
    val worldScale: Vec2; get() = ((parent?.scale ?: Vec2(1)) * size)

    val width; get() = worldScale.x
    val height; get() = worldScale.y

    infix fun collidesWith(other: CollisionBox2D): Boolean{
        return this.parent != other.parent && this.active && other.active && box intersects other.box
    }

    fun isColliding(collisions: Collection<CollisionBox2D> = parent?.objects?.flatMap { it.getAllCollisions() } ?: emptySet()): Boolean{
        for(c in collisions.toSet()) if(this collidesWith c) return true
        return false
    }

    fun getEjectionVector(other: CollisionBox2D): Vec2{
        return box overlapVector other.box
    }

    fun checkAllCollisions(obj: GameObject2D, movement: Vec2): Vec2{

        val collidedMove = movement.copy()

        // Create a temporary collision box in the new position to calculate collisions
        val newCollision = copy()
        newCollision.origin += (movement / obj.scale)

        // Iterate over all collision boxes sharing object collections and
        // eject this collision boxes object if the collision boxes collide
        for(box in obj.objects.flatMap { it.getAllCollisions() }){
            if(box != this) collidedMove plusAssign newCollision.getEjectionVector(box)
        }

        // If a collision is detected in either direction then set the velocity to 0
        if(collidedMove.x != movement.x) obj.velocity.x = 0f
        if(collidedMove.y != movement.y) obj.velocity.y = 0f

        return collidedMove
    }

    fun originWithParent(parent: GameObject2D): Vec2{
        return Vec2.fromMat4Translation(parent.transform.model * relModel)
    }
}