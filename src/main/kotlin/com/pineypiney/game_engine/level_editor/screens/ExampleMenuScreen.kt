package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.level_editor.openMenu
import com.pineypiney.game_engine.level_editor.setMenu
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL46C.glClearColor

class ExampleMenuScreen(gameEngine: PixelEngine) : MenuScreen("main menu", gameEngine) {

    private val title = SizedStaticText("PIXEL GAME!", window, 50, Vec2(2, 0.5), Vec4(0, 0, 0 ,1))

    private val startButton = TextButton("Create New Level", Vec2(-0.4, 0.0), Vec2(0.8, 0.25), window) {
        setMenu(NewLevelScreen(this, gameEngine), false)
        openMenu()
    }

    private val openButton = TextButton("Open Level", Vec2(-0.4, -0.3), Vec2(0.8, 0.25), window) {
        setMenu(SelectLevelScreen(this, gameEngine), false)
        openMenu()
    }

    private val optionsButton = TextButton("Options", Vec2(-0.4, -0.6), Vec2(0.8, 0.25), window) {
        setMenu(OptionsMenuScreen(this, gameEngine), false)
        openMenu()
    }

    private val exitButton = TextButton("Close", Vec2(-0.4, -0.9), Vec2(0.8, 0.25), window) {
        window.shouldClose = true
    }

    private val testButton = TextButton("Test", Vec2(0.5, -0.9), Vec2(0.4, 1.15), window) {
        setMenu(PauseMenuScreen(gameEngine))
        openMenu()
    }

    override fun addObjects() {
        super.addObjects()

        add(startButton)
        add(openButton)
        add(optionsButton)
        add(exitButton)
        add(testButton)
    }

    override fun initInteractables() {
        super.initInteractables()
        glClearColor(1f, 0f, 0f, 1f)

        title.init()
    }

    override fun render(tickDelta: Double) {
        super.render(tickDelta)
        title.drawCentered(Vec2(0, 0.6))
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        title.updateAspectRatio(window)
    }

    override fun cleanUp() {
        super.cleanUp()
        title.delete()
    }
}