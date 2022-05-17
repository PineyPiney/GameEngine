package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.Window

interface Visual {

    var visible: Boolean

    fun updateAspectRatio(window: Window) {}
}