package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.objects.LateParse
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.fields.ComponentField
import com.pineypiney.game_engine.util.ByteData

class PrefabComponentAddEdit(parentLoc: String, val head: String, val data: String, val list: LateParse?) : PrefabEdit(parentLoc) {
	override fun execute(obj: GameObject) {
		val c = findDescendant(obj) ?: return
		if(list != null) {
			GameObjectSerializer.parseComponent(head.byteInputStream(Charsets.ISO_8859_1), data.byteInputStream(Charsets.ISO_8859_1), list, c)
		}
		else {
			val list = mutableListOf<Triple<ComponentI, ComponentField<*>, String>>()
			GameObjectSerializer.parseComponent(head.byteInputStream(Charsets.ISO_8859_1), data.byteInputStream(Charsets.ISO_8859_1), list, c)
			for ((comp, field, str) in list) field.set(str, comp)
		}
	}

	override fun serialise(head: StringBuilder, data: StringBuilder) {
		head.append("CPAD" + ByteData.int2String(this.head.length) + this.head + ByteData.int2String(this.data.length))
		data.append(this.data)
	}
}