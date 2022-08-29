package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.resources.Media
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.i
import java.nio.ByteBuffer
import java.nio.ShortBuffer

class Video(val name: String, val attributes: VideoAttributes, val images: Array<ByteBuffer>, val audioSamples: Array<ShortBuffer>): Media() {

    var loop: Boolean = false
        set(value) {
            field = value
            audio.audio.loop = value
        }

    val textureData = VideoImages(this)
    val audio = VideoAudio(this)

    val length: Double get() = attributes.length
    val frameRate: Double get() = attributes.frameRate
    val frameNumber: Int get() = attributes.frameNumber
    val frameTime: Double get() = 1 / frameRate
    val width: Int get() = attributes.width
    val height: Int get() = attributes.height
    val pixelFormat: Int get() = attributes.pixelFormat
    val sampleRate: Int get() = attributes.sampleRate

    var timeStamp = 0.0

    var currentFrameTime = 0L
    var lastTextureGrabTime = 0.0

    init {
//        textureData.init()
//        audio.init()
    }

    fun getCurrentTexture(): Texture{
        // Update the time stamp
        updateTimeStamp()
        return textureData.current
    }

    fun updateTimeStamp(){

        val time = Timer.frameTime
        if(status == MediaStatus.PLAYING && timeStamp < length){
            timeStamp += (time - lastTextureGrabTime)
            if(timeStamp >= length){
                if(loop) {
                    timeStamp -= length
                    currentFrameTime = 0L
                    textureData.reset()
                    audio.reset()
                }
                else{
                    timeStamp = length
                    status = MediaStatus.PAUSED
                }
            }

            textureData.update()
            //audio.update()
            lastTextureGrabTime = time
        }
    }

    fun imageAt(time: Double): ByteBuffer{
        return image((time * frameRate).i)
    }

    fun image(index: Int): ByteBuffer{
        return images[index]
    }

    fun audio(index: Int): ShortBuffer{
        return audioSamples[index]
    }

    override fun play(volume: Float) {
        timeStamp = 0.0
        lastTextureGrabTime = Timer.frameTime
        status = MediaStatus.PLAYING
        audio.audio.rewind()
        //audio.audio.play()
    }

    override fun pause() {
        status = MediaStatus.PAUSED
        audio.audio.pause()
    }

    override fun resume() {
        lastTextureGrabTime = Timer.frameTime
        status = MediaStatus.PLAYING
        audio.audio.play()
    }

    override fun stop() {
        timeStamp = 0.0
        status = MediaStatus.STOPPED
        audio.audio.stop()
    }

    override fun delete() {
        textureData.delete()
    }

    companion object{
        //val broke = Video(Texture.broke, AudioSource(Audio.broke), 1.0, 1.0, 1)
    }
}