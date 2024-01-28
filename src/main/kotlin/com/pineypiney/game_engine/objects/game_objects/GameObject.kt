package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform
import com.pineypiney.game_engine.objects.util.components.Component
import glm_.mat4x4.Mat4

abstract class GameObject : Initialisable, Storable {

    override var objects: ObjectCollection? = null
    override val components: MutableSet<Component> = mutableSetOf()
    override val children: MutableSet<Storable> = mutableSetOf()

    override var name: String = "GameObject"
    abstract val transform: Transform<*, *, *>
    override var parent: GameObject? = null

    val relativeModel: Mat4 get() = transform.model
    val worldModel: Mat4 get() = parent?.let { it.worldModel * relativeModel } ?: relativeModel

    override fun init() {}


    fun addChild(vararg child: Storable){
        this.children.addAll(child)
    }
    fun addChildren(children: Iterable<Storable>){
        this.children.addAll(children)
    }
    fun removeChild(vararg child: Storable){
        this.children.removeAll(child.toSet())
    }
    fun removeChildren(children: Iterable<Storable>){
        this.children.removeAll(children.toSet())
    }


    override fun addTo(objects: ObjectCollection){
        objects.gameItems.add(this)
    }

    override fun removeFrom(objects: ObjectCollection) {
        objects.gameItems.remove(this)
    }

    inline fun <reified E> getComponent(): E?{
        return components.firstOrNull { it is E } as? E
    }

    override fun delete() {
        objects?.gameItems?.remove(this)
    }
}