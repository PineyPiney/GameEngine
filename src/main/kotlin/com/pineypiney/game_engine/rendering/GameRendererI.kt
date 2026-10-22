package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponentI

interface GameRendererI<in E : GameLogicI> : RendererI {

	fun render(game: E, tickDelta: Double)



	fun renderLayer(layer: Int, game: E, tickDelta: Double) = renderLayer(game.gameObjects[layer], tickDelta) { -(transformComponent.worldPosition - viewPos).length2() }

	fun <C : Comparable<C>> renderLayer(layer: Int, game: E, tickDelta: Double, sort: GameObject.() -> C) = renderLayer(game.gameObjects[layer], tickDelta, sort)

	fun renderLayer(layer: Collection<GameObject>, tickDelta: Double) = renderLayer(layer, tickDelta) { -(transformComponent.worldPosition - viewPos).length2() }

	fun <C : Comparable<C>> renderLayer(layer: Collection<GameObject>, tickDelta: Double, sort: GameObject.() -> C) {
		val sortedObjects = layer.flatMap { it.catchRenderingComponents() }.sortedBy(sort)
		for (o in sortedObjects) {
			renderObject(o, tickDelta)
		}
	}

	fun renderObject(obj: GameObject, tickDelta: Double) {
		val renderedComponents = obj.components.filterIsInstance<RenderedComponentI>().filter { it.visible }
		if (renderedComponents.isNotEmpty()) {
			for (c in obj.components.filterIsInstance<PreRenderComponent>()) c.preRender(this, tickDelta)
			for (c in renderedComponents) c.render(this, tickDelta)
		} else for (c in obj.components.filterIsInstance<PreRenderComponent>()) {
			if (!c.whenVisible) c.preRender(this, tickDelta)
		}
	}

}