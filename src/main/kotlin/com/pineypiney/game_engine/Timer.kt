package com.pineypiney.game_engine

import glm_.d

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
        var time: Double = 0.0; private set
        var delta: Double = 0.0; private set
        var frameTime: Double = 0.0; private set
        var frameDelta: Double = 0.0; private set

        fun getCurrentTime(): Double{
            return System.nanoTime().d/1000000000.0
        }
    }
}