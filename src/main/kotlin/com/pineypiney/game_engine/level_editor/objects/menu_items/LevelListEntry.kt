package com.pineypiney.game_engine.level_editor.objects.menu_items

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.level_editor.PixelWindow
import com.pineypiney.game_engine.level_editor.resources.levels.LevelDetails
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.ScrollerText
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.SelectableScrollingListEntry
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class LevelListEntry(parent: LevelList, number: Int, val details: LevelDetails): SelectableScrollingListEntry<LevelList>(parent, number){

    private val nameText = ScrollerText(details.worldName, PixelWindow.INSTANCE, size * Vec2(0.5, 0.4), limits, defaultColour)
    private val createdText = ScrollerText("Created: ${details.created.format(LevelDetails.formatter)}", PixelWindow.INSTANCE, size * Vec2(0.4, 0.3), limits, defaultColour)
    private val editedText = ScrollerText("Edited: ${details.edited.format(LevelDetails.formatter)}", PixelWindow.INSTANCE, size * Vec2(0.4, 0.3), limits, defaultColour)

    override fun init() {
        super.init()

        nameText.init()
        createdText.init()
        editedText.init()
    }

    override fun draw() {
        super.draw()
        nameText.drawCenteredLeft(relative(0.05, 0.75))
        createdText.drawCenteredLeft(relative(0.05, 0.25))
        editedText.drawCenteredLeft(relative(0.55, 0.25))
    }

    override fun select() {
        super.select()
        nameText.colour = selectedColour
    }

    override fun unselect() {
        super.unselect()
        nameText.colour = defaultColour
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        nameText.updateAspectRatio(window)
        createdText.updateAspectRatio(window)
        editedText.updateAspectRatio(window)
    }

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        // Only unselect when letting go of the mouse button,
        // giving time for the start/delete buttons to function
        if(!this.hover && this.forceUpdate && action == GLFW.GLFW_RELEASE) unselect()
        return super.onPrimary(game, action, mods, cursorPos)
    }

    override fun delete() {
        super.delete()
        nameText.delete()
        createdText.delete()
        editedText.delete()
    }

    companion object {
        val defaultColour = Vec4(0, 0, 0, 1)
        val selectedColour = Vec4(1)
    }
}