package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete

abstract class AbstractResourceLoader<E: Deleteable>: Deleteable{

    protected val map: MutableMap<ResourceKey, E> = mutableMapOf()
    abstract val missing: E

    operator fun get(key: ResourceKey): E = map[key] ?: missing

    override fun delete() {
        map.delete()
        map.clear()
    }
}

