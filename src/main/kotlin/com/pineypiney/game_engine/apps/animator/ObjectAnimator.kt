package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine
import java.io.File

class ObjectAnimator(
	val resources: ResourcesLoader = FileResourcesLoader(File("src/main/resources")),
	creator: () -> GameObject,
	val tweaker: (GameObject) -> Unit = {}
) {

	private val engine = object : WindowedGameEngine<AnimatorLogic>(resources) {

		init {
			GameEngineI.defaultFont = "Large Font"

			// Create all the fonts
			FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
			FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader)
		}

		override val activeScreen: AnimatorLogic = AnimatorLogic(this, null, creator)

		var lastFrameTime = 0.0
		override val window: WindowI get() = Companion.window

		override fun init() {
			super.init()
			activeScreen.o?.let { tweaker(it) }
		}

		override fun render(tickDelta: Double) {
			super.render(tickDelta)
			val newTime = Timer.getCurrentTime() * 1000.0
			lastFrameTime = newTime
		}
	}

	fun run() {
		engine.run()
	}

	fun setAnimating(o: GameObject) {
		window.title = "${o.name} Animator"
		engine.activeScreen.setAnimating(o)
	}

	companion object {

		fun init() {
			window.init()
		}

		val window = object : Window("Animator", 960, 540, false, true) {
			override val input: Inputs = DefaultInput(this)
		}

		fun run(creator: () -> GameObject, tweaker: (GameObject) -> Unit = {}) {
			init()
			val animator = ObjectAnimator(creator = creator, tweaker = tweaker)
			animator.run()
		}
	}
}