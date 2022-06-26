package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.resources.Media
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.delete
import glm_.d
import glm_.i
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.min

class Video(grabber: FFmpegFrameGrabber): Media() {

    constructor(stream: InputStream): this(FFmpegFrameGrabber(stream))
    constructor(file: File): this(FFmpegFrameGrabber(file))
    constructor(filename: String): this(FFmpegFrameGrabber(filename))

    var timeStamp = 0.0
    var lastTextureGrabTime = 0.0

    val width: Int
    val height: Int
    val audioChannels: Int

    val length: Double
    val frameRate: Double
    val frameTime: Double

    val textures: List<Texture>

    init{
        grabber.start()

        this.width = grabber.imageWidth
        this.height = grabber.imageHeight
        this.audioChannels = grabber.audioChannels

        this.length = grabber.lengthInTime.d / 1000000
        this.frameRate = grabber.frameRate
        this.frameTime = 1000.0 / this.frameRate

        // https://www.tabnine.com/code/java/classes/org.bytedeco.javacv.FFmpegFrameGrabber?snippet=5921ec434002b00004d6afd4
        val recorder = FFmpegFrameRecorder("src\\main\\resources\\videos\\new.mp4", width, height, audioChannels)
        recorder.videoCodec = avcodec.AV_CODEC_ID_H264
        recorder.format = "mp4"
        recorder.frameRate = frameRate
        recorder.sampleFormat = grabber.sampleFormat
        recorder.sampleRate = grabber.sampleRate
        recorder.start()

        while (true) {
            try {
                val captured_frame = grabber.grabFrame()
                if (captured_frame == null) {
                    GameEngine.logger.warn("!!! Failed cvQueryFrame")
                    break
                }
                recorder.timestamp = grabber.timestamp
                recorder.record(captured_frame)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val t = mutableListOf<Texture>()
        while(grabber.hasVideo()){
            val frame: Frame? = grabber.grab()
            val image: Frame? = grabber.grabImage()
            val audio: Frame? = grabber.grabSamples()
            val key: Frame? = grabber.grabKeyFrame()

            val imageBuffer: ByteBuffer? = frame?.image?.get(0) as ByteBuffer?

            /*
            val frameStream = ByteArrayInputStream(image?.array() ?: continue)
            t.add(Texture(frameStream))

             */

            /*
            val data = frame?.data
            val array = data?.array()
            val frameStream = ByteArrayInputStream(array ?: continue)
            t.add(Texture(frameStream))
            */
        }

        textures = t.distinct()

        grabber.audioStream

        grabber.stop()
        grabber.release()
    }

    fun getCurrentTexture(): Texture{
        // Update the time stamp
        updateTimeStamp()

        val index = (this.timeStamp * this.frameRate).i
        return textures.getOrElse(index) {textures.lastOrNull() ?: Texture.brokeTexture}
    }

    fun updateTimeStamp(){
        val time = Timer.frameTime
        if(this.status == MediaStatus.PLAYING && this.timeStamp < this.length){
            this.timeStamp = min(timeStamp + (time - this.lastTextureGrabTime), this.length)
            this.lastTextureGrabTime = time
            if(this.timeStamp == this.length) this.status = MediaStatus.PAUSED
        }
    }

    override fun play(volume: Float) {
        timeStamp = 0.0
        this.status = MediaStatus.PLAYING
    }

    override fun pause() {
        this.status = MediaStatus.PAUSED
    }

    override fun resume() {
        this.status = MediaStatus.PLAYING
    }

    override fun stop() {
        timeStamp = 0.0
        this.status = MediaStatus.STOPPED
    }

    override fun delete() {
        textures.delete()
    }
}