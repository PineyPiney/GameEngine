package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.level_editor.PixelWindow
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class InspectorField(val fieldName: String, override val parent: Inspector,
                     override var origin: Vec2, override val size: Vec2,
                     default: String,
                     allowedChars: List<Char> = Inspector.num,
                     val action: (Inspector, String, String) -> Unit):
    InteractableMenuItem() {

    val text = SizedStaticText("$fieldName: ", PixelWindow.INSTANCE, size.y * 320, size * Vec2(1))
    val field = ActionTextField<ActionTextField<*>>(relative(0.6, 0), Vec2(size * Vec2(0.4, 1)), PixelWindow.INSTANCE){ field, _, _ ->
        action(parent, fieldName.lowercase().replace(' ', '_'), field.text)
    }

    init {
        field.allowed = allowedChars
        field.text = default
    }

    override fun init() {
        super.init()
        text.init()
    }

    override fun setChildren() {
        super.setChildren()
        addChild(field)
    }

    override fun draw() {
        text.drawCenteredLeft(relative(0, 0))
        field.draw()
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        text.updateAspectRatio(window)
    }

    override fun delete() {
        super.delete()
        text.delete()
    }
}