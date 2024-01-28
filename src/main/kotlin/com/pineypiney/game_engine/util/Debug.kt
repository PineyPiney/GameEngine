package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import glm_.d

class Debug {

    val times = mutableListOf<Double>()

    fun start(): Debug{
        times.clear()
        times.add(millis())
        return this
    }

    fun add(){
        times.add(millis())
    }

    fun printDiffs(){
        if(times.size <= 1) return
        val b = StringBuilder("Times are ${times[1] - times[0]}")
        for(i in (2..<times.size)) b.append(", ${times[i] - times[i - 1]}")

        GameEngineI.logger.debug(b.toString())
    }

    companion object{
        fun millis() = System.nanoTime().d / 1000000.0
        fun micros() = System.nanoTime().d / 1000.0
    }
}