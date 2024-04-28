package com.pineypiney.game_engine.util.input

class PS4Controller(id: Int, inputs: Inputs): GamePad(id, inputs) {

	override fun updateBonusButtons(buttons: ByteArray) {
		super.updateBonusButtons(buttons)

		// Touch Pad
		updateButton(15, buttons[13])
		// Controller only registers the mute button if it is connected with a wire
		if(numButtons == 19) {
			updateButton(16, buttons[14])
		}
	}
}