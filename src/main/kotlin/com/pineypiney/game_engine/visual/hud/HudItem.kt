package com.pineypiney.game_engine.visual.hud

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.visual.menu_items.InteractableMenuItem
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey

abstract class HudItem : InteractableMenuItem() {

    abstract val game: IGameLogic

    companion object {
        val HUDShader = ShaderLoader.getShader(ResourceKey("vertex\\2D"), ResourceKey("fragment\\texture"))
    }
}