package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.PixelEngine

abstract class SubMenuScreen(backgroundName: String, val parent: MenuScreen, gameEngine: PixelEngine): MenuScreen(backgroundName, gameEngine) {

}