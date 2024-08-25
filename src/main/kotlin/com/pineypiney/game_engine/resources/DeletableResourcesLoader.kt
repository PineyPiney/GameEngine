package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.util.extension_functions.delete

abstract class DeletableResourcesLoader<E : Deleteable> : AbstractResourceLoader<E>() {

	override fun delete() {
		map.delete()
		super.delete()
	}
}