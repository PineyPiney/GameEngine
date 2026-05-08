package com.pineypiney.game_engine.objects.prefabs

import com.google.gson.JsonElement
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.util.serialisation.Codec

class PrefabComponentRemoveEdit(val component: String) : PrefabEdit() {

	override fun execute(obj: GameObject, parentLoc: String, list: LateParse<JsonElement>) {
		val c = findDescendant(obj, parentLoc) ?: return
		val comp = c.components.firstOrNull { it.id == component } ?: return
		comp.delete()
		c.components.remove(comp)
	}

	override fun getID(): String = "cprm"

	companion object {
		val CODEC = Codec.STRING.map("comp", PrefabComponentRemoveEdit::component, ::PrefabComponentRemoveEdit)
	}
}