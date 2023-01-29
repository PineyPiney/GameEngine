package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

class CheckBox(override var size: Vec2, val action: (Boolean) -> Unit): InteractableMenuItem() {

    var ticked = false

    override var shader: Shader = transparentTextureShader

    val background = TextureLoader.getTexture(ResourceKey("menu_items\\check_box\\background"))
    val check = TextureLoader.getTexture(ResourceKey("menu_items\\check_box\\check"))

    override fun draw() {

        shader.use()
        shader.setUniforms(uniforms)

        background.bind()
        shape.bindAndDraw()

        if(ticked){
            check.bind()
            shape.draw()
        }
    }

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        if(action == 1) toggle()
        return super.onPrimary(game, action, mods, cursorPos)
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        size = Vec2(0.1, 0.1 * window.aspectRatio)
    }

    fun toggle() {
        ticked = !ticked
        action(ticked)
    }
}