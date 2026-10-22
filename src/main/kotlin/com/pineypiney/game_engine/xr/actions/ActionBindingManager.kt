package com.pineypiney.game_engine.xr.actions

import com.pineypiney.game_engine.xr.XrUtil
import org.lwjgl.openxr.XrAction
import org.lwjgl.openxr.XrActionSuggestedBinding
import org.lwjgl.openxr.XrInstance
import org.lwjgl.system.MemoryStack

@Suppress("UNUSED")
open class ActionBindingManager(val instance: XrInstance) {

	val khrSimpleList = mutableListOf<Pair<XrAction, String>>()
	val oculusTouchList = mutableListOf<Pair<XrAction, String>>()

	val map = mutableMapOf(
		"/interaction_profiles/khr/simple_controller" to khrSimpleList,
		"/interaction_profiles/oculus/touch_controller" to oculusTouchList,
	)

	fun bindMenu(action: InputAction<*, *>): ActionBindingManager {
		action.addBindings(instance, khrSimpleList, "/input/menu/click")
		action.addBindings(instance, oculusTouchList, "/input/menu/click")
		return this
	}
	fun bindSystem(action: InputAction<*, *>): ActionBindingManager {
		action.addBindings(instance, oculusTouchList, "/input/system/click")
		return this
	}
	fun bindButtonLeftA(action: InputAction<*, *>): ActionBindingManager{
		action.addBindings(instance, oculusTouchList, "/input/x/click")
		return this
	}
	fun bindButtonLeftB(action: InputAction<*, *>): ActionBindingManager{
		action.addBindings(instance, oculusTouchList, "/input/y/click")
		return this
	}
	fun bindButtonRightA(action: InputAction<*, *>): ActionBindingManager{
		action.addBindings(instance, oculusTouchList, "/input/a/click")
		return this
	}
	fun bindButtonRightB(action: InputAction<*, *>): ActionBindingManager{
		action.addBindings(instance, oculusTouchList, "/input/b/click")
		return this
	}

	fun bindGrip(action: InputAction<*, *>): ActionBindingManager {
		action.addBindings(instance, khrSimpleList, "/input/select/click")
		action.addBindings(instance, oculusTouchList, "/input/squeeze/value")
		return this
	}
	fun bindTrigger(action: InputAction<*, *>): ActionBindingManager {
		action.addBindings(instance, oculusTouchList, "/input/trigger/value")
		return this
	}
	fun bindThumbstick(action: Vec2Action): ActionBindingManager{
		action.addBindings(instance, oculusTouchList, "/input/thumbstick")
		return this
	}
	fun bindThumbstickX(action: InputAction<*, *>): ActionBindingManager{
		action.addBindings(instance, oculusTouchList, "/input/thumbstick/x")
		return this
	}
	fun bindThumbstickY(action: InputAction<*, *>): ActionBindingManager{
		action.addBindings(instance, oculusTouchList, "/input/thumbstick/y")
		return this
	}
	fun bindGripPose(action: PoseAction): ActionBindingManager{
		action.addBindings(instance, khrSimpleList, "/input/grip/pose")
		action.addBindings(instance, oculusTouchList, "/input/grip/pose")
		return this
	}
	fun bindAimPose(action: PoseAction): ActionBindingManager{
		action.addBindings(instance, khrSimpleList, "/input/aim/pose")
		action.addBindings(instance, oculusTouchList, "/input/aim/pose")
		return this
	}
	fun bindHaptic(action: VibrationAction): ActionBindingManager{
		action.addBindings(instance, khrSimpleList, "/output/haptic")
		action.addBindings(instance, oculusTouchList, "/output/haptic")
		return this
	}

	fun bind(action: OpenXrAction<*, *>, profile: String, path: String): ActionBindingManager{
		action.addBindings(instance, map.getOrPut(profile, ::mutableListOf), path)
		return this
	}

	fun bind(stack: MemoryStack): Map<String, XrActionSuggestedBinding.Buffer> {
		return map.mapValues { (_, profile) -> XrUtil.createSuggestedBindings(stack, profile) }
	}
}