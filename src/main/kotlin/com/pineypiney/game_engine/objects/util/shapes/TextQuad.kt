package com.pineypiney.game_engine.objects.util.shapes

import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2
import org.lwjgl.opengl.GL11C.GL_TRIANGLE_FAN

class TextQuad(floats: FloatArray, val texture: Texture, val offset: Vec2) : ArrayShape(floats, intArrayOf(2, 2)){

    override fun bind() {
        super.bind()
        texture.bind()
    }

    override fun draw(mode: Int) {
        super.draw(GL_TRIANGLE_FAN)
    }

    override fun drawInstanced(amount: Int, mode: Int) {
        super.drawInstanced(amount, GL_TRIANGLE_FAN)
    }

    companion object{

    }
}
