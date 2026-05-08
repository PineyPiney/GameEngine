package com.pineypiney.game_engine.objects.prefabs

import com.google.gson.JsonElement
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.util.serialisation.Codec

class PrefabChildRemoveEdit(val child: String) : PrefabEdit() {

	override fun execute(obj: GameObject, parentLoc: String, list: LateParse<JsonElement>) {
		val d = findDescendant(obj, parentLoc) ?: return
		val c = d.children.firstOrNull { it.name == child } ?: return
		d.removeAndDeleteChild(c)
	}

	override fun getID(): String = "chrm"

	companion object {
		val CODEC: Codec<PrefabChildRemoveEdit> = Codec.STRING.map("child", PrefabChildRemoveEdit::child, ::PrefabChildRemoveEdit)
	}
}