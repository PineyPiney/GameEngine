package com.pineypiney.game_engine.objects.prefabs

import com.google.gson.JsonElement
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.util.serialisation.Codec
import com.pineypiney.game_engine.util.serialisation.JsonOps

class PrefabFieldEdit(val field: String, val data: JsonElement) : PrefabEdit() {

	override fun execute(obj: GameObject, parentLoc: String, list: LateParse<JsonElement>) {
		val c = findDescendant(obj, parentLoc) ?: return
		if(field.length > 1) {
			val (component, field) = c.getComponentAndField(field) ?: return
			if (field.isLateParse()) list.add(Triple(component, field, data))
			else field.set(JsonOps, data)
		}
		else when(field[0]){
			'l' -> c.layer = data.asInt
			'a' -> c.active = data.asBoolean
		}
	}

	override fun getID(): String = "fled"

	companion object {
		val CODEC = Codec.map(
			Codec.STRING.field("field", PrefabFieldEdit::field),
			Codec.serial(JsonOps).field("json", PrefabFieldEdit::data),
			::PrefabFieldEdit
		)
	}
}