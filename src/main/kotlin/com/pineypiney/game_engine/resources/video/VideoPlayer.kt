package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.util.I
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class VideoPlayer(val video: Video, override var origin: Vec2 = Vec2(-1, -1), override val size: Vec2 = Vec2(2, 2)): Initialisable,
    Drawable {

    override var visible: Boolean = true

    override fun init() {

    }

    override fun draw() {


        video.getCurrentTexture().bind()

        shader.use()

        var model = glm.translate(I, Vec3(origin.x, origin.y, 0))
        model = model.scale(Vec3(size.x, size.y, 1))
        shader.setMat4("model", model)

        shape.bind()
        shape.draw()
    }

    fun play() = video.play()
    fun pause() = video.pause()
    fun resume() = video.resume()
    fun stop() = video.stop()

    override fun delete() {
        video.delete()
    }

    companion object{
        val shape = ArrayShape.cornerSquareShape3D
        val shader = MenuItem.opaqueTextureShader
    }
}