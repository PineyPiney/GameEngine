package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.ByteData

class PrefabFieldEdit(parentLoc: String, val field: String, val value: String) : PrefabEdit(parentLoc) {
	override fun execute(obj: GameObject) {
		val c = findDescendant(obj) ?: return
		c.setProperty(field, value)
	}

	override fun serialise(head: StringBuilder, data: StringBuilder) {
		head.append("FLED" + ByteData.int2String(field.length, 2) + field + ByteData.int2String(value.length))
		data.append(value)
	}
}