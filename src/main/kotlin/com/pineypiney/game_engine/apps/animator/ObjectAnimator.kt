package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.OpenGlResourceFactory
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.window.DefaultGLWindow
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine
import java.io.File

class ObjectAnimator(
	val resources: ResourcesLoader = FileResourcesLoader(OpenGlResourceFactory(), File("src/main/resources")),
	creator: () -> GameObject,
	val tweaker: (GameObject) -> Unit = {}
) {

	private val engine = object : WindowedGameEngine<AnimatorLogic>(resources) {

		override lateinit var activeScreen: AnimatorLogic

		var lastFrameTime = 0.0
		override val window: WindowI get() = Companion.window

		override fun loadResources() {
			super.loadResources()

			GameEngineI.defaultFont = "Large Font"

			// Create all the fonts
			FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
			FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader)
		}

		override fun setLogic() {
			activeScreen = AnimatorLogic(this, null, creator)
		}

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

		val window = DefaultGLWindow("Animator", 960, 540)

		fun run(creator: () -> GameObject, tweaker: (GameObject) -> Unit = {}) {
			init()
			val animator = ObjectAnimator(creator = creator, tweaker = tweaker)
			animator.run()
		}
	}
}