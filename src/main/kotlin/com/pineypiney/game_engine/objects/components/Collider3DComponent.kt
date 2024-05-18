package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.GameObject2D
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import glm_.quat.Quat
import glm_.vec3.Vec3

class Collider3DComponent(parent: GameObject, val box: Cuboid): Component(parent, "C3D") {

    constructor(parent: GameObject2D): this(parent, Cuboid(Vec3(), Quat.identity, Vec3(1)))

    val transformedBox get() = box transformedBy parent.worldModel
    var active = true

    override val fields: Array<Field<*>> = arrayOf(
        Vec3Field("ogn", box::center){ o -> box.center = o },
        Vec3Field("sze", box::size){ s -> box.size = s },
        QuatField("rtn", box::rotation){ r -> box.rotation = r},
        BooleanField("atv", ::active){ a -> active = a}
    )

    infix fun collidesWith(other: Collider3DComponent): Boolean{
        return this.parent != other.parent && active && other.active && box intersects other.box
    }

    fun isColliding(collisions: Collection<Collider3DComponent>? = parent.objects?.getAll3DCollisions()): Boolean{
        if(collisions.isNullOrEmpty()) return false
        for(c in collisions.toSet()) if(this collidesWith c) return true
        return false
    }

    fun checkAllCollisions(movement: Vec3): Vec3{

        val collidedMove = movement.copy()

        // Create a temporary collision box in the new position to calculate collisions
        val newCollision = transformedBox
        newCollision.center plusAssign movement

        // Iterate over all collision boxes sharing object collections and
        // eject this collision boxes object if the collision boxes collide
        for(collider in parent.objects?.getAll3DCollisions() ?: emptySet()){
            if(collider != this) {
                val overlap = newCollision.getEjection(collider.transformedBox, movement)
                if(overlap.x != 0f || overlap.y != 0f || overlap.z != 0f) {
                    newCollision.center plusAssign overlap
                    collidedMove plusAssign overlap
                }
            }
        }

        // If a collision is detected in either direction then set the velocity to 0
        parent.velocity = Vec3(
            if((collidedMove.x < movement.x && parent.velocity.x > 0) || (collidedMove.x > movement.x && parent.velocity.x < 0)) 0f else parent.velocity.x,
            if((collidedMove.y < movement.y && parent.velocity.y > 0) || (collidedMove.y > movement.y && parent.velocity.y < 0)) 0f else parent.velocity.y,
            if((collidedMove.z < movement.z && parent.velocity.z > 0) || (collidedMove.z > movement.z && parent.velocity.z < 0)) 0f else parent.velocity.z,
        )

        return collidedMove
    }

    fun isGrounded(): Boolean{
        val b = transformedBox
        b.center.y -= 0.01f

        return (parent.objects?.getAll3DCollisions()?.minus(this))?.any { it.transformedBox.intersects(b) } ?: false
    }
}