package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import glm_.vec2.Vec2

abstract class HudItem : InteractableMenuItem() {

    abstract val game: GameLogicI

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        super.onPrimary(game, action, mods, cursorPos)
        return if(hover) Interactable.INTERRUPT else action
    }
}