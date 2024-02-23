package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.resources.video.Video
import glm_.vec2.Vec2

abstract class AbstractVideoPlayer: Initialisable, Drawable {

    abstract val video: Video

    override var visible: Boolean = true
    val uniforms = shader.compileUniforms()

    override fun init() {
        uniforms.setMat4Uniform("model"){ model }
    }

    override fun draw() {
        val tex = video.getCurrentTexture()
        tex.bind()

        shader.use()
        //shader.setUniforms(uniforms,) Haha nobody cares about you no todo for yo- AHHH WHAT HAPPENED!

        shape.bindAndDraw()
    }

    fun play() = video.play()
    fun pause() = video.pause()
    fun resume() = video.resume()
    fun stop() = video.stop()

    override fun delete() {
        video.delete()
    }

    companion object{
        // Image must be flipped vertically
        val shape = SquareShape(Vec2(0.5), Vec2(1, -1))
        val shader = MenuItem.opaqueTextureShader
    }
}