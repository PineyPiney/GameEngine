package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.level_editor.objects.menu_items.KeyBindList
import com.pineypiney.game_engine.level_editor.openMenu
import com.pineypiney.game_engine.level_editor.setMenu
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.util.input.InputState
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

class KeyBindsMenuScreen(parent: MenuScreen, gameEngine: PixelEngine) : SubMenuScreen("main menu", parent, gameEngine) {

    private val keys = KeyBindList(Vec2(-0.8, -0.5), Vec2(1.6, 1.3), 0.5f, 0.05f)

    private val backButton = TextButton("Back", Vec2(-0.4, -0.9), Vec2(0.8, 0.3), window) {
        setMenu(parent)
        openMenu(false)
    }

    override fun addObjects() {
        super.addObjects()

        add(keys)
        add(backButton)
    }

    override fun onInput(state: InputState, action: Int): Int {
        if(state.i == GLFW.GLFW_KEY_ESCAPE){
            if(action == 1) {
                if(keys.selectedEntry >= 0){
                    keys.getSelectedEntry()?.unselect()
                    keys.selectedEntry = -1
                    return Interactable.INTERRUPT
                }
            }
        }
        else{
            if(keys.selectedEntry > -1 && action == 0){
                keys.setKeyBind(state)
                return Interactable.INTERRUPT
            }
        }
        return super.onInput(state, action)
    }
}