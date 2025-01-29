package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.ByteData

class PrefabComponentRemoveEdit(parentLoc: String, val component: String) : PrefabEdit(parentLoc) {
	override fun execute(obj: GameObject) {
		val c = findDescendant(obj) ?: return
		val comp = c.components.firstOrNull { it.id == component } ?: return
		comp.delete()
		c.components.remove(comp)
	}

	override fun serialise(head: StringBuilder, data: StringBuilder) {
		head.append("CPRM" + ByteData.int2String(component.length, 1))
		data.append(component)
	}
}