package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import java.io.File

class Prefab(val file: File, val edits: MutableList<PrefabEdit> = mutableListOf()) : GameObject("$file Prefab") {

	constructor(fileLocation: String, edits: MutableList<PrefabEdit> = mutableListOf()): this(File("src/main/resources/$fileLocation.pfb"))

	fun parseAndEdit(): Prefab{
		parse(this)
		for (edit in edits) {
			edit.execute(this)
		}
		return this
	}

	fun parse(obj: GameObject? = null) = GameObjectSerializer.parse(file.inputStream(), obj)
}