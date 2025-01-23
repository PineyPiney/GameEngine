package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import java.io.File

class Prefab(val file: File, val edits: MutableList<PrefabEdit> = mutableListOf()) : GameObject("$file Prefab") {

	override fun init() {
		GameObjectSerializer.Companion.parse(file.inputStream(), this)
		for (edit in edits) {
			edit.execute(this)
		}
		super.init()
	}
}