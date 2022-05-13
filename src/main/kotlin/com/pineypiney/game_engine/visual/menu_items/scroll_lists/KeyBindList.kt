package com.pineypiney.game_engine.visual.menu_items.scroll_lists

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.KeyBind
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class KeyBindList(input: Inputs, origin: Vec2, size: Vec2, entryHeight: Float, scrollerWidth: Float) :
    SelectableScrollingListItem(input, origin, size, entryHeight, scrollerWidth) {

    override val items: List<ScrollingListEntry<*>> = createKeys()

    // Overriding this function allows it to return this specific class
    override fun getSelectedEntry(): KeyBindMenuEntry? = getEntry(selectedEntry)

    fun setKeyBind(bind: KeyBind){
        val entry = getSelectedEntry()

        entry?.setKeyBind(bind)

        selectedEntry = -1
    }

    override fun createKeys(): List<KeyBindMenuEntry> {
        val idList = input.keyBinds.keys.toList()
        val keys = idList.indices.map { i ->
            KeyBindMenuEntry(this, i, idList[i])
        }
        return keys.toList()
    }


    class KeyBindMenuEntry(parent: KeyBindList, number: Int, val key: ResourceKey): SelectableScrollingListEntry<KeyBindList>(parent, number) {

        private var input = parent.input
        private var binding: KeyBind = input.getKeyBinding(key)

        var text: ScrollerText = ScrollerText(binding.toString(), size * Vec2(0.3, 0.8), limits, Vec4())

        private fun updateText(){
            text = ScrollerText(binding.toString(), size * Vec2(0.3, 0.8), limits, Vec4(), shader = entryTextShader)
        }

        fun setKeyBind(bind: KeyBind){

            this.binding = bind
            updateText()

            input.setKeyBind(key, bind)
        }

        override fun select(){
            super.select()
            text.colour = Vec4(1, 1, 1, 1)
        }

        override fun unselect(){
            super.unselect()
            text.colour = Vec4(0, 0, 0, 1)
        }

        override fun draw() {
            super.draw()
            text.drawCenteredLeft(origin + (size * Vec2(0.05, 0.5)))
        }

        override fun updateAspectRatio(window: Window) {
            text.updateAspectRatio(window)
        }

        override fun delete() {
            text.delete()
        }
    }
}