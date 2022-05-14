package com.pineypiney.game_engine.resources.audio

import com.pineypiney.game_engine.util.ResourceKey
import java.util.logging.Logger
import javax.sound.sampled.*
import kotlin.math.max
import kotlin.math.min

// https://www.geeksforgeeks.org/play-audio-file-using-java/
class Audio(val stream: AudioInputStream): com.pineypiney.game_engine.resources.Media() {

    private val clip: Clip = AudioSystem.getClip()
    //val sampleRate = getFloatControl(FloatControl.Type.SAMPLE_RATE)

    var currentFrame: Long = 0

    init {
        try{
            clip.open(stream)
        }
        catch (e: Exception){
            clip.open(brokeAudio.stream)
        }
    }

    override fun play(volume: Float) {
        setGain(volume)
        clip.loop(1)
        this.status = MediaStatus.PLAYING
    }

    override fun pause(){
        if(status == MediaStatus.PAUSED){
            println("Audio already paused")
            return
        }

        this.currentFrame = this.clip.microsecondPosition
        this.clip.stop()
        this.status = MediaStatus.PAUSED
    }

    override fun resume(){
        if(status == MediaStatus.PLAYING){
            println("Audio already playing")
            return
        }
        this.clip.close()
        this.clip.microsecondPosition = this.currentFrame
        play()
    }

    override fun stop(){
        this.currentFrame = 0
        this.clip.stop()
        this.clip.close()
    }

    fun setMute(mute: Boolean) = setBoolControl(BooleanControl.Type.MUTE, mute)
    fun setReverb(reverb: Boolean) = setBoolControl(BooleanControl.Type.APPLY_REVERB, reverb)

    fun setGain(volume: Float) = setFloatControl(FloatControl.Type.MASTER_GAIN, volume)
    fun setVolume(volume: Float) = setFloatControl(FloatControl.Type.VOLUME, volume)
    fun setBalance(balance: Float) = setFloatControl(FloatControl.Type.BALANCE, balance)
    fun setPan(pan: Float) = setFloatControl(FloatControl.Type.PAN, pan)
    fun setSampleRate(pan: Float) = setFloatControl(FloatControl.Type.SAMPLE_RATE, pan)

    private fun setBoolControl(control: BooleanControl.Type, value: Boolean){
        getBoolControl(control)?.value = value
    }

    private fun setFloatControl(control: FloatControl.Type, value: Float){
        val controller = getFloatControl(control) ?: return
        controller.value = min(max(value, controller.minimum), controller.maximum)
    }

    private fun getBoolControl(control: BooleanControl.Type): BooleanControl?{
        try {
            return clip.getControl(control) as BooleanControl
        }
        catch(e: Exception){
            Logger.getLogger("Pixel Game").warning("Audio does not have boolean control type $control")
            e.printStackTrace()
        }
        return null
    }

    private fun getFloatControl(control: FloatControl.Type): FloatControl?{
        try {
            return clip.getControl(control) as FloatControl
        }
        catch(e: Exception){
            Logger.getLogger("Pixel Game").warning("Audio does not have float control type $control")
            e.printStackTrace()
        }
        return null
    }

    override fun delete() {

    }

    companion object{
        val brokeAudio: Audio; get() = AudioLoader.INSTANCE.getAudio(ResourceKey("broke"))
    }
}