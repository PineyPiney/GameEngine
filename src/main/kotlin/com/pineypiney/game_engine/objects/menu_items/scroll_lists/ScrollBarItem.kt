package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.objects.menu_items.InteractableMenuItem
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL46C.GL_TRIANGLES
import org.lwjgl.opengl.GL46C.glDrawArrays

class ScrollBarItem(val parent: ScrollingListItem, override var origin: Vec2 = Vec2(), override var size: Vec2 = Vec2()): InteractableMenuItem() {

    override var pressed: Boolean = false

    override fun draw() {

        shader.use()
        val model = glm.translate(I, Vec3(origin)).scale(Vec3(size))
        shader.setMat4("model", model)

        shape.bind()
        TextureLoader.getTexture(ResourceKey("menu_items/buttons/button")).bind()

        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    override fun update(interval: Float, time: Double) {

    }

    override fun onDrag(game: IGameLogic, cursorPos: Vec2, cursorDelta: Vec2) {
        super.onDrag(game, cursorPos, cursorDelta)
        forceUpdate = true

        // If the scroller item is taller, then the same scroll value should move the bar by a smaller amount
        // (Remember that parent.scroll is proportional, a value between 0 and (1-ratio))
        parent.scroll += (cursorDelta.y / (2 * parent.size.y))
    }

    override fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        val p = super.onPrimary(game, action, mods, cursorPos)

        if(!pressed) forceUpdate = false

        return p
    }
}