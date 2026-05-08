package com.pineypiney.game_engine.objects.prefabs

import com.google.gson.JsonElement
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.util.serialisation.Codec
import com.pineypiney.game_engine.util.serialisation.JsonOps

class PrefabComponentAddEdit(val json: JsonElement) : PrefabEdit() {

	override fun execute(obj: GameObject, parentLoc: String, list: LateParse<JsonElement>) {
		val c = findDescendant(obj, parentLoc) ?: return
		GameObjectSerializer.parseComponent(JsonOps, json, list, c)
	}

	override fun getID(): String = "cpad"

	companion object {
		val CODEC = Codec.serial(JsonOps).map("json", PrefabComponentAddEdit::json, ::PrefabComponentAddEdit)
	}
}