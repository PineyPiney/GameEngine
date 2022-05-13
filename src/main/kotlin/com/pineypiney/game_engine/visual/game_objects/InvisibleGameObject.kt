package com.pineypiney.game_engine.visual.game_objects

import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.visual.util.collision.CollisionBox
import com.pineypiney.game_engine.visual.util.collision.SoftCollisionBox
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

class InvisibleGameObject(position: Vec2 = Vec2(), scale: Vec2 = Vec2(1)) : GameObject() {

    override val id: ResourceKey = ResourceKey("barrier")

    override val collision: CollisionBox = SoftCollisionBox(this, Vec2(0), Vec2(1))

    init {
        this.position = position
        this.scale = scale
    }

    override fun render(vp: Mat4, tickDelta: Double) {
        collision.render(vp)
    }

    override fun toData(): Array<String> {
        var string ="GameItem: \n"

        if(shader != defaultShader) string += "\tShader: $shader\n"
        if(position != Vec2()) string += "\tPosition: $position\n"

        return arrayOf(string)
    }

    override fun copy(): InvisibleGameObject {
        return InvisibleGameObject(position.copy())
    }

    override fun delete() {
        objects.forEach { it.gameItems.remove(this) }
    }

    override fun toString(): String{
        return "GameItem[]"
    }
}