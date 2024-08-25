package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent

class FPSCounter(parent: GameObject, var period: Double, val callback: (Double) -> Unit = {}) :
	Component(parent, "FPS"),
	PreRenderComponent {

	override val whenVisible: Boolean = false
	var FPS: Double = .0
	var tally: Int = 0
	var nextUpdate: Double = .0

	override val fields: Array<Field<*>> = arrayOf(
		DoubleField("prd", ::period) { period = it },
		DoubleField("FPS", ::FPS) { FPS = it },
		IntField("tly", ::tally) { tally = it },
		DoubleField("nxt", ::nextUpdate) { nextUpdate = it },
	)

	override fun preRender(tickDelta: Double) {
		tally++
		if (Timer.time > nextUpdate) {
			FPS = tally / period
			nextUpdate = Timer.time + (nextUpdate + period - Timer.time) % period
			callback(FPS)
			tally = 0
		}

	}
}