package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.*
import com.pineypiney.game_engine.level_editor.objects.menu_items.LevelList
import com.pineypiney.game_engine.level_editor.objects.menu_items.LevelListEntry
import com.pineypiney.game_engine.level_editor.resources.levels.LevelDetails
import com.pineypiney.game_engine.level_editor.resources.levels.LevelLoader
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.menu_items.TextButton
import glm_.vec2.Vec2

class SelectLevelScreen(parent: MenuScreen, gameEngine: PixelEngine): SubMenuScreen("main menu", parent, gameEngine) {

    var selectedLevel: LevelDetails? = null

    private val levelList = LevelList(Vec2(-0.8, -0.5), Vec2(1.6, 1.3), 0.5f, 0.05f){ i, entry ->
        if(i >= 0){
            startButton.active = true
            deleteButton.active = true
            selectedLevel = (entry as LevelListEntry).details
        }
        else{
            startButton.active = false
            deleteButton.active = false
        }
    }

    private val startButton = TextButton("Start", Vec2(-0.9, -0.725), Vec2(0.8, 0.2), window){
        selectedLevel?.let { details ->
            val openedLevel = createLevel(details)
            setGame(openedLevel)
            openGame()
        }
    }

    private val deleteButton = TextButton("Delete", Vec2(0.1, -0.725), Vec2(0.8, 0.2), window){
        selectedLevel?.let { details ->
            deleteLevel(details)
        }
    }

    private val backButton = TextButton("Back", Vec2(-0.9, -0.95), Vec2(1.8, 0.2), window){
        setMenu(parent)
        openMenu(false)
    }

    override fun addObjects() {
        super.addObjects()

        add(levelList)
        add(startButton)
        add(deleteButton)
        add(backButton)
    }

    override fun initInteractables() {
        super.initInteractables()

        levelList.importance = -1
        startButton.active = false
        deleteButton.active = false
    }

    private fun createLevel(details: LevelDetails): LevelMakerScreen {
        val newLevel = LevelMakerScreen(
            gameEngine,
            details.worldName,
            details.width,
            details.created
        )
        newLevel.colour = details.colour

        val objects = LevelLoader.INSTANCE.loadLevel(details)
        if(objects != null){
            newLevel.gameObjects.gameItems.clear()
            newLevel.gameObjects.gameItems.addAll(objects)

            newLevel.editorSidebar.updateLayers(*(objects.filterIsInstance<GameObject2D>().map { it.depth }.toSet().toIntArray()))
        }


        return newLevel
    }

    private fun deleteLevel(details: LevelDetails){

        levelList.removeEntryIf { entry ->
            details.worldName == (entry).details.worldName
        }

        LevelLoader.INSTANCE.deleteLevel(details)
    }
}