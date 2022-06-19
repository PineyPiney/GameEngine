package com.pineypiney.game_engine.util

class ResourceKey(key: String) {

    val key: String

    init {
        this.key = key.replace('\\', '/')
            .replace('/', '/')
            .replace(';', '/')
    }

    // Equals is used for any other occasions where it doesn't use the HashCode
    override fun equals(other: Any?): Boolean {
        if(other is ResourceKey) return this.key == other.key
        return false
    }

    // HashCodes are how maps are searched, so it's important that two different ResourceKeys with the same
    // name are considered identical by maps
    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun toString(): String {
        return "ResourceKey[$key]"
    }
}