package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.ByteData

class PrefabChildRemoveEdit(parentLoc: String, val child: String) : PrefabEdit(parentLoc) {
	override fun execute(obj: GameObject) {
		val d = findDescendant(obj) ?: return
		val c = d.children.firstOrNull { it.name == child } ?: return
		d.removeAndDeleteChild(c)
	}

	override fun serialise(head: StringBuilder, data: StringBuilder) {
		head.append("CHRM" + ByteData.int2String(child.length, 1))
		data.append(child)
	}
}