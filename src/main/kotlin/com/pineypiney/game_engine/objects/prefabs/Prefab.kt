package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import java.io.File

class Prefab(val file: File, val edits: MutableList<Pair<String, PrefabEdit>> = mutableListOf()) : GameObject("$file Prefab") {

	constructor(fileLocation: String, edits: MutableList<Pair<String, PrefabEdit>> = mutableListOf()) : this(File("src/main/resources/$fileLocation.pfb"), edits)

	fun parseAndEdit(): Prefab{
		parse(this)
		return this
	}

	fun parse(obj: GameObject? = null) = GameObjectSerializer.parse(file, obj)
}