package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.level_editor.LevelMakerScreen
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.menu_items.TextField
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.window.WindowI
import glm_.c
import glm_.f
import glm_.i
import glm_.vec2.Vec2

class Inspector(override val game: LevelMakerScreen, val item: GameObject2D): Sidebar() {

    override var origin: Vec2 = Vec2(1 - width, -1)

    val inspectorText = SizedStaticText("Inspector", game.window, 40, size * Vec2(1, 0.15))

    val posXField = InspectorField("Pos X", this, relative(0.1, 0.8), size * Vec2(0.8, 0.03), item.position.x.toString(), action = Inspector::parseInspector)
    val posYField = InspectorField("Pos Y", this, relative(0.1, 0.75), size * Vec2(0.8, 0.03), item.position.y.toString(), action = Inspector::parseInspector)
    val depthField = InspectorField("Depth", this, relative(0.1, 0.7), size * Vec2(0.8, 0.03), item.depth.toString(), TextField.integers.map { it.c } + '-', Inspector::parseInspector)
    val rotField = InspectorField("Rotation", this, relative(0.1, 0.65), size * Vec2(0.8, 0.03), item.rotation.toString(), action = Inspector::parseInspector)
    val sizeXField = InspectorField("Size X", this, relative(0.1, 0.6), size * Vec2(0.8, 0.03), item.scale.x.toString(), action = Inspector::parseInspector)
    val sizeYField = InspectorField("Size Y", this, relative(0.1, 0.55), size * Vec2(0.8, 0.03), item.scale.y.toString(), action = Inspector::parseInspector)

    override fun init() {
        super.init()

        inspectorText.init()
    }

    override fun setChildren() {
        super.setChildren()
        addChild(posXField, posYField, depthField, rotField, sizeXField, sizeYField)
    }

    override fun draw() {
        super.draw()

        posXField.draw()
        posYField.draw()
        depthField.draw()
        rotField.draw()
        sizeXField.draw()
        sizeYField.draw()

        inspectorText.drawCenteredTop(relative(0.5, 1))
    }

    private fun parseInspector(variable: String, value: String){
        if(value.filter { it != '-' && it != '.' } != ""){
            try{
                when(variable){
                    "pos_x" -> {
                        item.position = Vec2(value.f, item.position.y)
                    }
                    "pos_y" -> {
                        item.position = Vec2(item.position.x, value.f)
                    }
                    "depth" -> {
                        val oldDepth = item.depth
                        item.depth = value.i

                        if(oldDepth != item.depth) game.editorSidebar.updateLayers(oldDepth, item.depth)
                    }
                    "rotation" -> {
                        item.rotation = value.f
                    }
                    "size_x" -> {
                        item.scale = Vec2(value.f, item.scale.y)
                    }
                    "size_y" -> {
                        item.scale = Vec2(item.scale.x, value.f)
                    }
                }
            }
            catch (e: NumberFormatException) { e.printStackTrace() }
        }
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        inspectorText.updateAspectRatio(window)
    }

    override fun delete() {
        super.delete()
        inspectorText.delete()
    }

    companion object {
        val num = TextField.numbers.map { it.c } + '-'
    }
}