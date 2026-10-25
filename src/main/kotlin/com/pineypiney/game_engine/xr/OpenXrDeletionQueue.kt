package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.DeletionQueue

class OpenXrDeletionQueue : DeletionQueue() {

	fun <E> pushHandle(obj: E, func: (E) -> Int): OpenXrDeletionQueue {
		deletables.add(OpenXrContainer(obj, func))
		return this
	}

	class OpenXrContainer<E>(val obj: E, val func: (E) -> Int) : Deletable {
		override fun delete() {
			XrUtil.processResult(func(obj), "Failed to destroy $obj")
		}
	}
}