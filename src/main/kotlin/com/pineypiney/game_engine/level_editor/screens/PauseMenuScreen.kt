package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.level_editor.openGame
import com.pineypiney.game_engine.level_editor.openMenu
import com.pineypiney.game_engine.level_editor.setMenu
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.util.input.InputState
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL46C

class PauseMenuScreen(gameEngine: PixelEngine) : MenuScreen("pause menu.png", gameEngine) {

    val resumeButton = TextButton("Resume", Vec2(-0.4, 0.2), Vec2(0.8, 0.4), window) {
        openGame(false)
    }

    val optionsButton = TextButton("Options", Vec2(-0.4, -0.5), Vec2(0.8, 0.3), window) {
        setMenu(OptionsMenuScreen(this, gameEngine), false)
        openMenu()
    }

    val exitButton = TextButton("Exit", Vec2(-0.4, -0.9), Vec2(0.8, 0.3), window) {
        setMenu(ExampleMenuScreen(gameEngine))
        openMenu()
    }

    override fun addObjects() {
        super.addObjects()

        add(resumeButton)
        add(optionsButton)
        add(exitButton)
    }

    override fun initInteractables(){
        super.initInteractables()
        GL46C.glClearColor(0f, 1f, 0f, 1f)
    }

    override fun onInput(state: InputState, action: Int): Int {
        if(super.onInput(state, action) == Interactable.INTERRUPT) return Interactable.INTERRUPT

        if(action == GLFW.GLFW_RELEASE) {
            if (state.i == GLFW.GLFW_KEY_ESCAPE) {
                openGame(false)
                return Interactable.INTERRUPT
            }
        }

        return action
    }
}