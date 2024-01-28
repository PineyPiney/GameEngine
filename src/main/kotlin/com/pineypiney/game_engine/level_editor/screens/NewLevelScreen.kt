package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.*
import com.pineypiney.game_engine.level_editor.resources.levels.LevelLoader
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.menu_items.TextField
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.window.WindowI
import glm_.c
import glm_.f
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import java.time.LocalDateTime

class NewLevelScreen(parent: MenuScreen, gameEngine: PixelEngine) : SubMenuScreen("main menu", parent, gameEngine) {

    private val title = SizedStaticText("Create New Level!", window, 50, Vec2(2, 0.5), Vec4(0, 0, 0 ,1))

    private val nameTextBox = TextField(Vec2(-0.5, -0.1), Vec2(0.6, 0.3), window)
    private val widthTextBox = TextField(Vec2(0.2, -0.1), Vec2(0.3, 0.3), window)

    private val createButton = TextButton("Create Level", Vec2(-0.4, -0.5), Vec2(0.8, 0.3), window) {
        val w =
            try{
                widthTextBox.text.f
            }
            catch(e: NumberFormatException){
                PixelEngine.logger.warn("Could not use width ${widthTextBox.text}, creating Level with width 50")
                50f
            }

        val level = createWorld(nameTextBox.text, w)
        setGame(level)
        openGame()
    }

    private val backButton = TextButton("Back", Vec2(-0.4, -0.9), Vec2(0.8, 0.3), window) {
        setMenu(parent)
        openMenu(false)
    }

    override fun addObjects() {
        super.addObjects()

        add(nameTextBox)
        add(widthTextBox)
        add(createButton)
        add(backButton)
    }

    override fun initInteractables() {
        super.initInteractables()

        title.init()

        val levels = LevelLoader.getAllLevels()
        var name = "New World"
        var i = 1
        while(levels.any { details -> details.worldName == name }){
            name = "New World $i"
            i++
        }

        nameTextBox.text = name
        widthTextBox.text = "50"
        widthTextBox.allowed = TextField.numbers.map { it.c }
    }

    private fun createWorld(name: String, width: Float) =
        LevelMakerScreen(
            gameEngine,
            name,
            width.coerceAtLeast(10 * gameEngine.window.aspectRatio),
            LocalDateTime.now()
        )

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        title.updateAspectRatio(window)
    }

    override fun render(tickDelta: Double) {
        super.render(tickDelta)
        title.drawCentered(Vec2(0, 0.6))
    }

    override fun cleanUp() {
        super.cleanUp()
        title.delete()
    }
}