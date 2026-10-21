package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.xr.OpenXrManager
import com.pineypiney.game_engine.xr.OpenXrSession
import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.XR10
import org.lwjgl.openxr.XrActionSet
import org.lwjgl.openxr.XrActionSuggestedBinding
import org.lwjgl.openxr.XrInstance
import org.lwjgl.system.MemoryStack

open class OpenXrActionSet(val name: String, val displayName: String, val priority: Int = 0) : Deletable {
	lateinit var actionSet: XrActionSet

	val inputActions = mutableListOf<InputAction<*, *>>()
	val outputActions = mutableListOf<OutputAction<*, *>>()

	open fun addActions(stack: MemoryStack, instance: XrInstance) {

	}

	fun init(stack: MemoryStack, openxr: OpenXrManager) {
		actionSet = XrUtil.createActionSet(stack, openxr.instance, name, displayName, priority)
		openxr.nameObject(actionSet.address(), XR10.XR_OBJECT_TYPE_ACTION_SET, "OpenXR Action Set")
		addActions(stack, openxr.instance)

		for(action in inputActions){
			action.init(stack, actionSet)
			action.name(openxr)
		}
		for(action in outputActions){
			action.init(stack, actionSet)
			action.name(openxr)
		}
	}

	fun <A: InputAction<*, *>> addAction(action: A): A {
		inputActions.add(action)
		return action
	}

	fun <A: OutputAction<*, *>> addAction(action: A): A {
		outputActions.add(action)
		return action
	}

	open fun bindActions(stack: MemoryStack, instance: XrInstance): Map<String, XrActionSuggestedBinding.Buffer> = emptyMap()

	open fun bindToSession(stack: MemoryStack, session: OpenXrSession) {
		for(action in inputActions) action.bindToSession(stack, session)
	}

	override fun delete() {
		inputActions.delete()
		outputActions.delete()
	}
}