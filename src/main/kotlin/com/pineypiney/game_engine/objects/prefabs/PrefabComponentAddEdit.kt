package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.util.ByteData

class PrefabComponentAddEdit(parentLoc: String, val head: String, val data: String) : PrefabEdit(parentLoc) {
	override fun execute(obj: GameObject) {
		val c = findDescendant(obj) ?: return
		GameObjectSerializer.parseComponent(head.byteInputStream(Charsets.ISO_8859_1), data.byteInputStream(Charsets.ISO_8859_1), c)
	}

	override fun serialise(head: StringBuilder, data: StringBuilder) {
		head.append("CPAD" + ByteData.int2String(this.head.length) + this.head + ByteData.int2String(this.data.length))
		data.append(this.data)
	}
}