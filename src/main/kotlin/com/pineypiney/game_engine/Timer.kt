package com.pineypiney.game_engine

class Timer {

    fun init(){
        time = getCurrentTime()
        frameTime = getCurrentTime()
    }

    fun tick(): Double{
        val newTime = getCurrentTime()
        delta = newTime - time
        time = newTime
        return delta
    }

    fun tickFrame(): Double{
        val newTime = getCurrentTime()
        frameDelta = newTime - frameTime
        frameTime = newTime
        return frameDelta
    }

    companion object{
        var startTime = System.nanoTime()
        var time: Double = 0.0; private set
        var delta: Double = 0.0; private set
        var frameTime: Double = 0.0; private set
        var frameDelta: Double = 0.0; private set

        // Gets the current system time in seconds
        fun getCurrentTime(): Double{
            return (System.nanoTime() - startTime).toDouble() * 1e-9
        }

        fun getCurrentMillis(): Double = (System.nanoTime() - startTime).toDouble() * 1e-6
    }
}