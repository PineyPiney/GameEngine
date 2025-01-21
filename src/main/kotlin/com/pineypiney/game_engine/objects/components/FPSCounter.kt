package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.text.Text

class FPSCounter(parent: GameObject, var period: Double, val callback: (Double) -> Unit = {}) :
	Component(parent, "FPS"),
	PreRenderComponent {

	override val whenVisible: Boolean = false
	var FPS: Double = .0
	var tally: Int = 0
	var nextUpdate: Double = .0

	override fun preRender(tickDelta: Double) {
		tally++
		if (Timer.time > nextUpdate) {
			FPS = tally / period
			nextUpdate = Timer.time + (nextUpdate + period - Timer.time) % period
			callback(FPS)
			tally = 0
		}

	}

	companion object {
		fun createCounterWithText(obj: GameObject, period: Double, format: String, params: Text.Params): GameObject{
			val textChild = Text.makeMenuText("FPS: 0.0", params)
			obj.addChild(textChild)
			obj.components.add(FPSCounter(obj, period){
				textChild.getComponent<TextRendererComponent>()?.text?.text = format.replace("$", it.toString())
			})
			return obj
		}
	}
}