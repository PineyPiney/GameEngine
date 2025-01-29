package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.GameObjectSerializer
import com.pineypiney.game_engine.util.ByteData

class PrefabChildAddEdit(parentLoc: String, val child: String) : PrefabEdit(parentLoc) {
	override fun execute(obj: GameObject) {
		val c = findDescendant(obj) ?: return
		c.addChild(GameObjectSerializer.parse(child.byteInputStream(Charsets.ISO_8859_1)))
	}

	override fun serialise(head: StringBuilder, data: StringBuilder) {
		head.append("CHAD" + ByteData.int2String(child.length))
		data.append(child)
	}
}