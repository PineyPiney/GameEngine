package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.ApiManager
import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.fonts.FontLoader
import com.pineypiney.game_engine.window.*

class ObjectAnimator(
	manager: ApiManager,
	val resources: ResourcesLoader = ResourcesLoader.directory(manager.createResourceFactory()),
	creator: () -> GameObject,
	val tweaker: (GameObject) -> Unit = {}
) {

	private val engine = object : WindowedGameEngine<AnimatorLogic>(manager, resources) {

		override lateinit var activeScreen: AnimatorLogic

		var lastFrameTime = 0.0
		override val window: Window get() = Companion.window

		override fun loadResources() {
			super.loadResources()

			GameEngineI.defaultFont = "Simplified Hans Light"

			// Create all the fonts
			FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader)
		}

		override fun setLogic() {
			activeScreen = AnimatorLogic(this, null, creator)
			deletionQueue.push(activeScreen)
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

		val window: Window
		init {
			val windowApi = GLFWManager
			windowApi.init()
			val properties = WindowProperties().opengl(4, 1)
				.addFlag(WindowFlags.RESIZABLE).addFlag(WindowFlags.TRANSPARENT)
				.visible(true).decorated(true).vSync(true)

			window = windowApi.createWindow("OpenXR Test", 1920, 1080, properties)
		}

		fun init() {
			window.init()
		}

		fun run(manager: ApiManager, creator: () -> GameObject, tweaker: (GameObject) -> Unit = {}) {
			init()
			val animator = ObjectAnimator(manager, creator = creator, tweaker = tweaker)
			animator.run()
		}
	}
}