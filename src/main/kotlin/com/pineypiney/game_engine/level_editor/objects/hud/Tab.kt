package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.objects.menu_items.AbstractButton
import glm_.vec2.Vec2

class Tab(override var origin: Vec2, override val size: Vec2, override val action: (button: AbstractButton) -> Unit): AbstractButton(){

}
