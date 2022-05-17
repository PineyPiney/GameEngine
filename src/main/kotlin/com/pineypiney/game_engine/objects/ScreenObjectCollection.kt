package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.game_objects.RenderedGameObject
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.collision.CollisionBox
import com.pineypiney.game_engine.util.extension_functions.delete

class ScreenObjectCollection {

    var backgrounds: MutableList<Drawable> = mutableListOf()

    var gameItems: MutableSet<GameObject> = mutableSetOf()

    val guiItems = mutableSetOf<MenuItem>()

    fun addObject(o: Storable?){
        // Add the object to this
        o?.addTo(this)

        // Add this to the object
        o?.objects?.add(this)
    }
    fun removeObject(o: Storable?){
        // Add the object to this
        o?.removeFrom(this)

        // Add this to the object
        o?.objects?.remove(this)
    }


    fun update(interval: Float){
        val time = Timer.time
        for(o in getAllObjects().filterIsInstance(Updateable::class.java)){
            o.update(interval, time)
        }
    }

    fun getAllObjects(): List<IScreenObject?>{
        val x = MutableList<IScreenObject?>(0){null}
        // Add gui items first so that they are interacted with first
        x.addAll(guiItems)
        x.addAll(gameItems)
        return x
    }

    fun getAllCollisions(): Set<CollisionBox>{
        return getAllObjects().filterIsInstance<Collidable>().map { it.collider }.toSet()
    }

    fun isHudHovered(): Boolean{
        return guiItems.filterIsInstance<Interactable>().any { it.hover }
    }

    fun forEachItem(action: (it: GameObject) -> Unit){
        gameItems.forEach { action.invoke(it) }
    }

    fun forEachRendered(action: (it: RenderedGameObject) -> Unit){
        gameItems.filterIsInstance<RenderedGameObject>().forEach { action.invoke(it) }
    }

    fun forEachCollision(action: (it: CollisionBox) -> Unit){
        getAllCollisions().forEach { action.invoke(it) }
    }

    fun firstItem(predicate: (GameObject) -> Boolean): GameObject?{
        return try{
            gameItems.first(predicate)
        }
        catch(e: NoSuchElementException){
            null
        }
    }

    fun delete(){
        getAllObjects().delete()
    }
}