package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xy

class ColliderComponent(parent: GameObject, val box: Rect2D): Component("C2D", parent) {

    constructor(parent: GameObject2D): this(parent, Rect2D(Vec2(), Vec2(1)))

    val transformedBox get() = box transformedBy parent.worldModel
    var active = true

    override val fields: Array<Field<*>> = arrayOf(
        Vec2Field("ogn", box::origin){ o -> box.origin = o },
        Vec2Field("sze", box::size){ s -> box.size = s },
        FloatField("rtn", box::angle){ r -> box.angle = r},
        BooleanField("atv", ::active){ a -> active = a}
    )

    infix fun collidesWith(other: ColliderComponent): Boolean{
        return this.parent != other.parent && active && other.active && box intersects other.box
    }

    fun isColliding(collisions: Collection<ColliderComponent>? = parent.objects?.getAllCollisions()): Boolean{
        if(collisions.isNullOrEmpty()) return false
        for(c in collisions.toSet()) if(this collidesWith c) return true
        return false
    }

    fun checkAllCollisions(movement: Vec2): Vec2{

        val collidedMove = movement.copy()

        // Create a temporary collision box in the new position to calculate collisions
        val newCollision = transformedBox
        newCollision.origin plusAssign (movement / parent.scale.xy)

        // Iterate over all collision boxes sharing object collections and
        // eject this collision boxes object if the collision boxes collide
        for(collider in parent.objects?.getAllCollisions() ?: emptySet()){
            if(collider != this) {
                val overlap = newCollision overlapVector collider.transformedBox
                if(overlap.x != 0f || overlap.y != 0f) {
                    collidedMove plusAssign overlap
                }
            }
        }

        // If a collision is detected in either direction then set the velocity to 0
        parent.velocity = Vec3(
            if((collidedMove.x < movement.x && parent.velocity.x > 0) || (collidedMove.x > movement.x && parent.velocity.x < 0)) 0f else parent.velocity.x,
            if((collidedMove.y < movement.y && parent.velocity.y > 0) || (collidedMove.y > movement.y && parent.velocity.y < 0)) 0f else parent.velocity.y,
            parent.velocity.z
        )

        return collidedMove
    }

    fun isGrounded(): Boolean{
        val b = transformedBox
        b.origin.y -= 0.01f

        return (parent.objects?.getAllCollisions()?.minus(this))?.any { it.transformedBox.intersects(b) } ?: false
    }
}