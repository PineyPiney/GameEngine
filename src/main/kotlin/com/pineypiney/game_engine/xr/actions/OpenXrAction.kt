package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.xr.OpenXrManager
import com.pineypiney.game_engine.xr.OpenXrSession
import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.XR10
import org.lwjgl.openxr.XrAction
import org.lwjgl.openxr.XrActionSet
import org.lwjgl.openxr.XrInstance
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.Struct

interface OpenXrAction<T, S: Struct<S>> : Deletable{

	val name: String
	val displayName: String

	val action: XrAction
	val set get() = action.actionSet
	val states: Map<Long, S>

	fun init(stack: MemoryStack, actionSet: XrActionSet)

	fun bindToSession(stack: MemoryStack, session: OpenXrSession){}

	fun name(manager: OpenXrManager){
		manager.nameObject(action.address(), XR10.XR_OBJECT_TYPE_ACTION, "OpenXR $displayName Action")
	}

	fun addBindings(instance: XrInstance, list: MutableList<Pair<XrAction, String>>, path: String){
		MemoryStack.stackPush().use { stack ->
			for((l, _) in states){
				list.add(action to (XrUtil.readPath(stack, instance, l) + path))
			}
		}
	}

	override fun delete() {
		for((_, state) in states) state.free()
		XrUtil.processResult(XR10.xrDestroyAction(action), "Failed to destroy OpenXR Action $displayName")
	}
}