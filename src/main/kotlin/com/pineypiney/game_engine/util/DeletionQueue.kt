package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.deleteArray
import com.pineypiney.game_engine.vulkan.VulkanManager
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkDevice

class DeletionQueue(val vulkan: VulkanManager) {

	val deletables = mutableListOf<Deletable>()

	fun push(function: () -> Unit): DeletionQueue {
		deletables.add(Container(function))
		return this
	}

	fun <E> push(obj: E, func: (VkDevice, E, VkAllocationCallbacks?) -> Unit): DeletionQueue {
		return push { func(vulkan.device.device, obj, null) }
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
}