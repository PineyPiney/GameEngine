package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.input.Inputs

abstract class GameLogic : GameLogicI {

	override val gameObjects: ObjectCollection = ObjectCollection()

	override fun init() {
		renderer.init()
		addObjects()
		for(layer in gameObjects.map) layer.value.init()
	}

	// This addObjects function allows adding items and calling init() on them all here in gameLogic, see init()
	abstract fun addObjects()

	override fun open() {
		// Force update everything, really don't think this is necessary because everything's being constantly updated anyway???
		//gameObjects.update(0f)

		// Reset textures so that the last bound texture isn't carried over
		Texture.broke.bind()
	}

	override fun update(interval: Float, input: Inputs) {
		gameObjects.update(interval)
	}

	override fun add(o: GameObject?) {
		gameObjects.addObject(o)
	}

	fun add(vararg os: GameObject?) {
		for (o in os) gameObjects.addObject(o)
	}

	fun addAll(os: Iterable<GameObject>) {
		gameObjects.addObjects(os)
	}

	override fun remove(o: GameObject?) {
		gameObjects.removeObject(o)
	}

	fun removeAll(os: Iterable<GameObject>) {
		gameObjects.removeObjects(os)
	}

	override fun cleanUp() {
		gameObjects.delete()
		renderer.delete()
	}
}