package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

open class ScrollingListEntry<E: ScrollingListItem>(val parent: E, val index: Int): MenuItem() {

    protected val limits = Vec2(parent.origin.y, parent.origin.y + parent.size.y)

    override var origin: Vec2 = Vec2()
    override val size = Vec2(parent.size.x * (1f - parent.scrollerWidth), parent.entryHeight)

    override val shader: Shader = entryShader

    override fun init() {}

    override fun delete() {}

    override fun setUniforms() {
        super.setUniforms()
        shader.setVec2("limits", limits)
    }

    override fun draw() {
        TextureLoader.getTexture(ResourceKey("broke")).bind()
        super.draw()
    }

    companion object{
        val entryShader = ShaderLoader.getShader(ResourceKey("vertex/scroll_entry"), ResourceKey("fragment/scroll_entry"))
        val entryTextShader = ShaderLoader.getShader(ResourceKey("vertex/scroll_entry"), ResourceKey("fragment/scroll_entry_text"))
    }
}