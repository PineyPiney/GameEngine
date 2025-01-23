package com.pineypiney.game_engine.objects.prefabs

import com.pineypiney.game_engine.objects.GameObject

abstract class PrefabEdit(val parentLoc: String) {

	fun findDescendant(obj: GameObject): GameObject?{
		if(parentLoc.isEmpty()) return obj
		val parts = parentLoc.split('.')
		var child = obj
		for(i in 0..<parts.size){
			child = child.getChild(parts[i]) ?: return null
		}
		return child
	}

	abstract fun execute(obj: GameObject)
}