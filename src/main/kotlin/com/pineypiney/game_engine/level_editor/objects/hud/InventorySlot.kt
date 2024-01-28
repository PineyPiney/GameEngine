package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.maths.I
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class InventorySlot(override val game: GameLogicI, override val parent: InventorySidebar, override var origin: Vec2, override var size: Vec2, val constructor: () -> RenderedGameObject2D): HudItem() {

    val item = constructor()
    var colour = Vec4(1, 0, 0, 1)

    override val shape: VertexShape = VertexShape.cornerSquareShape2D
    override var shader: Shader = translucentColourShader

    override fun init() {
        super.init()

        item.position = origin + Vec2(size.x/2, 0)
        item.scale = size
        item.setUniforms()
    }

    override fun setUniforms() {
        super.setUniforms()
        uniforms.setVec4Uniform("colour"){ colour }
    }

    override fun draw() {
        super.draw()

        item.render(I, I, 0.0)
    }

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        super.onPrimary(game, action, mods, cursorPos)

        this.colour.r = if(action == 0) 1f else 0.8f

        if(action == GLFW.GLFW_RELEASE){
            parent.game.selectedItem = constructor().apply { init() }
            parent.game.holding = true
            parent.game.grabPoint = Vec2()
            parent.game.state = com.pineypiney.game_engine.level_editor.LevelMakerScreen.PLACING_NEW
            return Interactable.INTERRUPT
        }

        return action
    }
}