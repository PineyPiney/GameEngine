package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.I
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL46C.GL_TRIANGLES
import org.lwjgl.opengl.GL46C.glDrawArrays

abstract class Button : StaticInteractableMenuItem() {

    var baseTexture: Texture = TextureLoader.blank()
    var hoverTexture: Texture = TextureLoader.blank()
    var clickTexture: Texture = TextureLoader.blank()

    var active: Boolean = true
    abstract val action: (button: Button) -> Unit

    override fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val ret = super.onPrimary(game, action, mods, cursorPos)
        if(ret == GLFW.GLFW_RELEASE && this.active){
            this.action.invoke(this)
        }
        return ret
    }

    private fun getCurrentTexture() : Texture {
        return when(true){
            pressed -> clickTexture
            hover -> hoverTexture
            else -> baseTexture
        }
    }

    override fun draw() {
        shape.bind()

        getCurrentTexture().bind()

        shader.use()

        var model = glm.translate(I, Vec3(origin.x, origin.y, 0))
        model = model.scale(Vec3(size.x, size.y, 1))
        shader.setMat4("model", model)

        glDrawArrays(GL_TRIANGLES, 0, 6)
    }
}