package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import org.lwjgl.system.CustomBuffer
import org.lwjgl.system.Struct

open class DeletionQueue {

	val deletables = mutableListOf<Deletable>()

	fun push(deletable: Deletable): DeletionQueue {
		deletables.add(deletable)
		return this
	}

	fun push(struct: Struct<*>): DeletionQueue {
		deletables.add(StructContainer(struct))
		return this
	}

	fun push(buffer: CustomBuffer<*>): DeletionQueue {
		deletables.add(StructBufferContainer(buffer))
		return this
	}

	fun <E> push(obj: E, function: (E) -> Unit): DeletionQueue {
		deletables.add(Container(obj, function))
		return this
	}

	fun push(function: () -> Unit): DeletionQueue {
		deletables.add(FuncContainer(function))
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

	class Container<E>(val obj: E, val func: (E) -> Unit) : Deletable {
		override fun delete() {
			func(obj)
		}
	}

	class FuncContainer(val func: () -> Unit) : Deletable {
		override fun delete() {
			func()
		}
	}

	class StructContainer(val struct: Struct<*>) : Deletable {
		override fun delete() {
			struct.free()
		}
	}

	class StructBufferContainer(val buffer: CustomBuffer<*>) : Deletable {
		override fun delete() {
			buffer.free()
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