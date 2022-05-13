package com.pineypiney.game_engine.visual

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.visual.game_objects.GameObject
import com.pineypiney.game_engine.visual.menu_items.MenuItem
import com.pineypiney.game_engine.visual.util.collision.CollisionBox

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
        return getAllObjects().filterIsInstance<GameObject>().map { it.collision }.toSet()
    }

    fun isHudHovered(): Boolean{
        return guiItems.filterIsInstance<Interactable>().any { it.hover }
    }

    fun forEachItem(action: (it: GameObject) -> Unit){
        gameItems.forEach { action.invoke(it) }
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