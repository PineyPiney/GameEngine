package com.pineypiney.game_engine.level_editor

import com.pineypiney.game_engine.level_editor.screens.MenuScreen
import com.pineypiney.game_engine.window.WindowGameLogic
import java.time.LocalDateTime

fun main() {
    gameEngine.run()
}

var gameEngine = PixelEngine(PixelWindow.INSTANCE)
val startTime: LocalDateTime = LocalDateTime.now()

fun setGame(game: WindowGameLogic, delete: Boolean = true){
    gameEngine.setGame(game, delete)
}
// Delete is set false when making a sub menu, and the current menu
// will be brought back once the player backs out of that menu
fun setMenu(newMenu: MenuScreen, delete: Boolean = true){
    gameEngine.setMenu(newMenu, delete)
}
fun openGame(init: Boolean = true){
    gameEngine.openGame(init)
}
fun openMenu(init: Boolean = true){
    gameEngine.openMenu(init)
}