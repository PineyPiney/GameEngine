package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.deleteArray

open class DeletionQueue {

	val deletables = mutableListOf<Deletable>()

	fun push(function: () -> Unit): DeletionQueue {
		deletables.add(Container(function))
		return this
	}

	fun push(deletable: Deletable): DeletionQueue {
		deletables.add(deletable)
		return this
	}

	fun pushAll(vararg deletable: Deletable): DeletionQueue {
		deletables.addAll(deletable)
		return this
	}

	fun pushAll(deletableIterable: Iterable<Deletable>): DeletionQueue {
		deletables.add(IterableContainer(deletableIterable))
		return this
	}

	fun pushArray(deletableArray: Array<out Deletable>): DeletionQueue {
		deletables.add(ArrayContainer(deletableArray))
		return this
	}

	fun flush() {
		while (deletables.isNotEmpty()) {
			val next = deletables.removeLast()
			try {
				next.delete()
			} catch (e: Exception) {
				GameEngineI.logger.error("Failed to delete $next")
				e.printStackTrace()
			}
		}
	}

	class Container(val func: () -> Unit) : Deletable {
		override fun delete() {
			func()
		}
	}

	class IterableContainer(val iterable: Iterable<Deletable>) : Deletable {
		override fun delete() {
			iterable.delete()
		}
	}

	class ArrayContainer(val array: Array<out Deletable>) : Deletable {
		override fun delete() {
			array.deleteArray()
		}
	}

	companion object {

		var GLOBAL: DeletionQueue = DeletionQueue(); private set

		fun setGlobalQueue(queue: DeletionQueue) {
			GLOBAL.flush()
			GLOBAL = queue
		}
	}
}