package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.visual.Drawable
import com.pineypiney.game_engine.visual.IScreenObject
import com.pineypiney.game_engine.visual.menu_items.MenuItem
import com.pineypiney.game_engine.visual.util.shapes.Shape
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL46C

class VideoPlayer(val video: Video, override var origin: Vec2 = Vec2(-1, -1), override val size: Vec2 = Vec2(2, 2)): IScreenObject, Drawable {

    override var visible: Boolean = true

    override fun init() {

    }

    override fun draw() {

        shape.bind()

        video.getCurrentTexture().bind()

        shader.use()

        var model = glm.translate(I, Vec3(origin.x, origin.y, 0))
        model = model.scale(Vec3(size.x, size.y, 1))
        shader.setMat4("model", model)

        GL46C.glDrawArrays(GL46C.GL_TRIANGLES, 0, 6)
    }

    fun play() = video.play()
    fun pause() = video.pause()
    fun resume() = video.resume()
    fun stop() = video.stop()

    override fun delete() {
        video.delete()
    }

    companion object{
        val shape = Shape.cornerSquareShape
        val shader = MenuItem.menuShader
    }
}