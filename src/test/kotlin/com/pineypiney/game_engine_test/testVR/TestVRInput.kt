package com.pineypiney.game_engine_test.testVR

import com.pineypiney.game_engine.vr.InputVR
import java.io.File

class TestVRInput: InputVR(File("src/main/resources/vr/actions.json").absolutePath) {

    var actionSet = invalidSet; private set
    var leftTrigger = invalid; private set
    var leftJoystick = invalid; private set
    var leftGrip = invalid; private set
    
    var leftX = invalid; private set

    override fun init() {
        super.init()

        actionSet = getActionSetHandle("/actions/default")
        leftTrigger = getActionHandle("/actions/default/in/left_trigger")
        leftGrip = getActionHandle("/actions/default/in/left_gripbinary")
        leftJoystick = getActionHandle("/actions/default/in/left_thumbstick")
        leftX = getActionHandle("/actions/default/in/left_secondarybutton")
    }
}