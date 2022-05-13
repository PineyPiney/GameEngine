package com.pineypiney.game_engine.util

interface Copyable<E>: Cloneable {
    fun copy(): E
}