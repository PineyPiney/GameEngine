package com.pineypiney.game_engine.visual.menu_items

import com.pineypiney.game_engine.visual.ScreenObjectCollection
import com.pineypiney.game_engine.visual.Storable
import com.pineypiney.game_engine.visual.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.I
import org.lwjgl.opengl.GL46C.GL_TRIANGLES
import org.lwjgl.opengl.GL46C.glDrawArrays

class MenuBackground(val texture: Texture, override val shader: Shader = menuShader) : MenuItem(), Storable {

    override val shape: Shape = menuShape

    override fun init() {

    }

    override fun draw() {
        shape.bind()

        shader.use()
        shader.setMat4("model", I)

        texture.bind()

        glDrawArrays(GL_TRIANGLES, 0, shape.numVertices)
    }

    override fun addTo(objects: ScreenObjectCollection) {
        objects.backgrounds.add(this)
    }

    override fun removeFrom(objects: ScreenObjectCollection) {
        objects.backgrounds.add(this)
    }

    override fun delete() {
        if(shader != menuShader) shader.delete()
    }

    override fun toString(): String{
        return "BackgroundItem[Texture: $texture, Shader: $shader]"
    }
}