package com.pineypiney.game_engine.apps.editor.util.context_menus

class ContextMenuEntry<T>(val name: String, val children: Array<ContextMenuEntry<T>> = emptyArray(), val action: T.() -> Unit = {}) {

}