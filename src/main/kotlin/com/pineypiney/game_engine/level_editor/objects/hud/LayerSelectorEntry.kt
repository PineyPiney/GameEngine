package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.level_editor.PixelWindow
import com.pineypiney.game_engine.objects.MovableDrawable
import com.pineypiney.game_engine.objects.menu_items.CheckBox
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.InteractableScrollingListEntry
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.ScrollerText
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class LayerSelectorEntry(parent: LayerSelector, index: Int): InteractableScrollingListEntry<LayerSelector>(parent, index){

    val layerText = ScrollerText(index.toString(), PixelWindow.INSTANCE, size * Vec2(0.8, 0.5), limits, Vec4())

    private val checkBox = object : CheckBox(), MovableDrawable{

        override val size: Vec2 = Vec2(0.2, 0.2 * parent.game.window.aspectRatio) * this@LayerSelectorEntry.size.x
        override val action: (Boolean) -> Unit = { parent.toggleLayer(this@LayerSelectorEntry.index, it) }
    }

    override fun init() {
        super.init()

        layerText.init()

        checkBox.ticked = true
        checkBox.init()
        addChild(checkBox)
    }

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        super.onPrimary(game, action, mods, cursorPos)
        return if(hover) 1 else 0
    }

    override fun draw() {
        super.draw()

        layerText.drawCenteredRight(relative(0.7, 0.5))
        checkBox.drawCenteredLeft(relative(0.75, 0.5))
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        layerText.updateAspectRatio(window)
    }

    override fun delete() {
        super.delete()
        layerText.delete()
    }
}
