package com.pineypiney.game_engine.apps.editor.util.context_menus

import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import glm_.vec2.Vec2i

class ContextMenu<C: ContextMenu.Context>(val entries: Array<ContextMenuEntry<C>>) {

	open class Context(val settings: EditorSettings, val viewport: Vec2i)
}