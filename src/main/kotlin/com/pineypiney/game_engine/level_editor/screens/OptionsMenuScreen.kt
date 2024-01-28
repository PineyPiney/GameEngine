package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.level_editor.openMenu
import com.pineypiney.game_engine.level_editor.setMenu
import com.pineypiney.game_engine.objects.menu_items.TextButton
import glm_.vec2.Vec2

class OptionsMenuScreen(parent: MenuScreen, gameEngine: PixelEngine) : SubMenuScreen("main menu", parent, gameEngine) {

    val keys = TextButton("KeyBinds", Vec2(-0.4, 0.2), Vec2(0.8, 0.4), window) {
        setMenu(KeyBindsMenuScreen(this, gameEngine), false)
        openMenu()
    }

    val backButton = TextButton("Back", Vec2(-0.4, -0.9), Vec2(0.8, 0.3), window) {
        setMenu(parent)
        openMenu(false)
    }

    override fun addObjects() {
        super.addObjects()

        add(keys)
        add(backButton)
    }
}