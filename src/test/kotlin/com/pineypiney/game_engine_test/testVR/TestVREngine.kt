package com.pineypiney.game_engine_test.testVR

import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.vr.HMD
import com.pineypiney.game_engine.vr.VRGameEngine

class TestVREngine(screen: (TestVREngine) -> TestVRGame, ups: Int, fps: Int, resourcesLoader: ResourcesLoader = FileResourcesLoader()): VRGameEngine<TestVRGame>(resourcesLoader) {

	override val TARGET_UPS: Int = ups
	override val TARGET_FPS: Int = fps

	override val hmd: HMD = HMD()

	override val activeScreen: TestVRGame = screen(this)

	override val inputVR: TestVRInput = TestVRInput()

	override fun handleVRInput() {
		super.handleVRInput()

		inputVR.updateActionSet(inputVR.actionSet)
		activeScreen.handleVRInput()
	}
}