package com.pineypiney.game_engine.level_editor.objects.util

import com.pineypiney.game_engine.objects.Collidable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2D
import com.pineypiney.game_engine.objects.util.collision.HardCollisionBox

class BarrierObject: GameObject2D(), Collidable {

    override var name: String = "barrier"

    override val collider: CollisionBox2D = HardCollisionBox(this, position, scale)

}