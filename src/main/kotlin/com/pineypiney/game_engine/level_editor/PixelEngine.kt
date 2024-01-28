package com.pineypiney.game_engine.level_editor

import com.pineypiney.game_engine.level_editor.resources.levels.LevelLoader
import com.pineypiney.game_engine.level_editor.screens.ExampleMenuScreen
import com.pineypiney.game_engine.level_editor.screens.MenuScreen
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine
import mu.KotlinLogging

class PixelEngine(override val window: WindowI): WindowedGameEngine<WindowGameLogic>(FileResourcesLoader("src/main/resources")) {

    override var TARGET_FPS: Int = 1000
    override val TARGET_UPS: Int = 20

    var game: WindowGameLogic = ExampleMenuScreen(this)
    var menu: MenuScreen = ExampleMenuScreen(this)

    override var activeScreen: WindowGameLogic = menu

    override fun init() {
        super.init()

        val levels = resourcesLoader.getStreams().filter { it.key.startsWith("levels/") }
        LevelLoader.INSTANCE.loadLevels(levels)
    }

    fun setGame(game: WindowGameLogic, delete: Boolean = true){
        if(delete) this.game.cleanUp()
        this.game = game
    }
    // Delete is set false when making a sub menu, and the current menu
    // will be brought back once the player backs out of that menu
    fun setMenu(newMenu: MenuScreen, delete: Boolean = true){
        if(delete) menu.cleanUp()
        menu = newMenu
    }
    fun openGame(init: Boolean = true){
        if(init) game.init()
        game.open()
        activeScreen = game
    }
    fun openMenu(init: Boolean = true){
        if(init) this.menu.init()
        menu.open()
        activeScreen = menu
    }

    companion object{
        val logger = KotlinLogging.logger("Pixel Game Editor")
    }
}