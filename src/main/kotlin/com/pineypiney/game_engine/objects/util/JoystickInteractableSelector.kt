package com.pineypiney.game_engine.objects.util

import com.pineypiney.game_engine.objects.components.InteractorComponent
import glm_.vec2.Vec2

class JoystickInteractableSelector(val items: () -> Collection<InteractorComponent>) {


	fun selectFirstButton() {
		val buttons = items()
		if (buttons.isNotEmpty() && buttons.none { it.hover }) buttons.first().hover = true
	}

	fun selectButton(d: Vec2) {
		val buttons = items()
		if (buttons.isNotEmpty()) {
			val selectedButton = buttons.firstOrNull { it.hover }
			if (selectedButton == null) {
				buttons.first().hover = true
			} else if (buttons.size > 1) {
				val otherButtons = buttons - selectedButton
				val measures = otherButtons.associateBy {
					val vec =
						Vec2(it.parent.transformComponent.worldPosition - selectedButton.parent.transformComponent.worldPosition)
					val dir = d dot vec.normalize()
					vec to dir
				}

				val nextButton = measures.filter { it.key.second > .6f }
					.maxByOrNull { it.key.second / it.key.first.length2() }?.value

				if (nextButton != null) {
					selectedButton.hover = false
					nextButton.hover = true
				}
			}
		}
	}
}