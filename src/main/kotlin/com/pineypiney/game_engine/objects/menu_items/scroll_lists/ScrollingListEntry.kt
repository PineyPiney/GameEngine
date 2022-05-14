package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.text.Font
import com.pineypiney.game_engine.objects.Text
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class ScrollingListEntry<E: ScrollingListItem>(val parent: E, val index: Int): MenuItem() {

    protected val limits = Vec2(parent.origin.y, parent.origin.y + parent.size.y)

    override var origin: Vec2 = Vec2()
    override val size = Vec2(parent.size.x * (1f - parent.scrollerWidth), parent.entryHeight)

    override val shader: Shader = entryShader

    override fun init() {}

    override fun delete() {}

    override fun draw() {
        shader.use()

        setUniforms(shader)

        shape.bind()
        TextureLoader.getTexture(ResourceKey("test/Pesto")).bind()

        drawArrays()
    }

    protected fun setUniforms(shader: Shader) {
        val model = glm.translate(I, Vec3(origin)).scale(Vec3(size))
        shader.setMat4("model", model)
        shader.setVec2("limits", limits)
    }

    companion object{
        val entryShader = ShaderLoader.getShader(ResourceKey("vertex/scroll_entry"), ResourceKey("fragment/scroll_entry"))
        val entryTextShader = ShaderLoader.getShader(ResourceKey("vertex/scroll_entry"), ResourceKey("fragment/scroll_entry_text"))
    }

    class ScrollerText(
        text: String, bounds: Vec2 = Vec2(2, 2),
        private var limits: Vec2, colour: Vec4 = Vec4(1, 1, 1, 1),
        font: Font = Font.defaultFont,
        shader: Shader = entryTextShader, window: Window = Window.INSTANCE

    ): Text(text, colour, bounds.x, bounds.y, font, shader, window) {

        override fun setUniversalUniforms(shader: Shader) {
            super.setUniversalUniforms(shader)
            shader.setVec2("limits", limits)
        }

        override fun setIndividualUniforms(shader: Shader, index: Int) {
            super.setIndividualUniforms(shader, index)
            val q: TextQuad? = getQuad(index)
            if(q != null){
                val bottom = q.bottomRight.y
                // This Vec2 contains the bottom of the texture and the height
                shader.setVec2("texture_section", Vec2(bottom, q.topLeft.y - bottom))
            }
        }
    }
}