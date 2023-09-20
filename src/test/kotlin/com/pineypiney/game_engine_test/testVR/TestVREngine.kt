package com.pineypiney.game_engine_test.testVR

import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.vr.HMD
import com.pineypiney.game_engine.vr.VRGameEngine

class TestVREngine(resourcesLoader: ResourcesLoader, screen: (TestVREngine) -> TestVRGame): VRGameEngine<TestVRGame>(resourcesLoader) {

    override val hmd: HMD = HMD()

    override val activeScreen: TestVRGame = screen(this)

    override val inputVR: TestVRInput = TestVRInput()

    override fun handleVRInput() {
        super.handleVRInput()

        inputVR.updateActionSet(inputVR.actionSet)
        activeScreen.handleVRInput()
    }
}