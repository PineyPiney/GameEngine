package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.jna.LibScePad

interface TriggerEffectGamepad {

	fun setLeftTriggerEffect(triggerEffect: LibScePad.TriggerEffectType)
	fun setRightTriggerEffect(triggerEffect: LibScePad.TriggerEffectType)
	fun setTriggerEffects(leftTrigger: LibScePad.TriggerEffectType, rightTrigger: LibScePad.TriggerEffectType)
}