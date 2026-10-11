package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.Deletable

class EngineDeletionQueue : DeletionQueue() {

	fun push(logic: GameLogicI) = push(LogicDeleter(logic))

	private class LogicDeleter(val logic: GameLogicI) : Deletable {
		override fun delete() = logic.cleanUp()
	}
}