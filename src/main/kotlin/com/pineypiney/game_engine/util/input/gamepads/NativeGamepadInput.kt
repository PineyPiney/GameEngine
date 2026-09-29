package com.pineypiney.game_engine.util.input.gamepads

import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.jna.GameInput
import com.pineypiney.game_engine.util.jna.LibScePad
import glm_.asHexString

class NativeGamepadInput(inputs: Inputs) : GamepadInput(inputs) {

	val xboxGamepads = mutableSetOf<NativeXboxController>()
	val psGamepads = mutableListOf<NativePsController>()
	lateinit var psGamepad: NativePsController

	override val connectedGamepads: Set<GamePad> get() = xboxGamepads + psGamepads

	val connectCb = GameInput.ConnectCallback { p ->
		val controller = GameInput.Controller(p)
		val info = controller.info

		if (info.deviceFamily != GameInput.FAMILY_XBOX_360 && info.deviceFamily != GameInput.FAMILY_XBOX_ONE) return@ConnectCallback

		println("Controller Connected")
		println("\tName = ${info.displayName}")
		println("\tFamily = ${info.deviceFamily}")
		println("\tType = ${info.supportedInput.asHexString}")
		println("\tGuid = ${info.containerId.toHexString()}")

		val gamepad = NativeXboxController(this, controller)
		gamepadConnectCallback(gamepad)
		synchronized(xboxGamepads) {
			xboxGamepads.add(gamepad)
		}
	}
	val disconnectCb = GameInput.DisconnectCallback { device ->

		val gamepad = xboxGamepads.firstOrNull { it.controller.device == device } ?: return@DisconnectCallback
		val info = gamepad.controller.info

		if (info.deviceFamily != GameInput.FAMILY_XBOX_360 && info.deviceFamily != GameInput.FAMILY_XBOX_ONE) return@DisconnectCallback

		println("Controller Disconnected")
		println("\tName = ${info.displayName}")
		println("\tFamily = ${info.deviceFamily}")
		println("\tType = ${info.supportedInput.asHexString}")
		println("\tGuid = ${info.containerId.toHexString()}")


		gamepadDisconnectCallback(gamepad)
		GameInput.INSTANCE.releaseController(gamepad.controller.p)
		synchronized(xboxGamepads) {
			xboxGamepads.remove(gamepad)
		}
	}

	init {
		initGameInput()
		initScePad()
	}

	override fun update() {
		super.update()
		removeDisconnectedPsGamepads()
		psGamepad.input()
		addConnectedPsGamepad()
	}

	fun removeDisconnectedPsGamepads() {
		var i = 0
		while (i < psGamepads.size) {
			val gamepad = psGamepads[i]
			if (gamepad.connected) i++
			else {
				gamepadDisconnectCallback(gamepad)
				psGamepads.removeAt(i)
				LibScePad.INSTANCE.scePadClose(gamepad.pad)
				gamepad.delete()
			}
		}
	}

	fun addConnectedPsGamepad() {
		if (psGamepad.connected) {
			psGamepads.add(psGamepad)
			gamepadConnectCallback(psGamepad)
			psGamepads.sortBy(NativePsController::userId)
			val userId = psGamepads.withIndex().firstOrNull { (i, gp) -> gp.userId != i + 1 }?.index ?: (psGamepads.size + 1)
			psGamepad = NativePsController(this, userId, LibScePad.INSTANCE.scePadOpen(userId, 0, 0))
		}
	}

	fun initGameInput() {
		GameInput.processError(GameInput.INSTANCE.initInput(), "Failed to initialise GameInput")
		GameInput.INSTANCE.setConnectCallback(connectCb)
		GameInput.INSTANCE.setDisconnectCallback(disconnectCb)
	}

	fun initScePad() {

		val initParam = LibScePad.InitParam(true)
		println("Initialised LibScePad with result " + LibScePad.INSTANCE.scePadInit3(initParam.p))
		initParam.delete()

		psGamepad = NativePsController(this, 1, LibScePad.INSTANCE.scePadOpen(1, 0, 0))
	}

	override fun delete() {
		try {
			GameInput.INSTANCE.releaseInput()
		} catch (e: Exception) {

		}
		LibScePad.INSTANCE.scePadTerminate()
	}
}