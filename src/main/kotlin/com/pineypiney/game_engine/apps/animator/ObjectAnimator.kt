package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.text.FontLoader
import com.pineypiney.game_engine.util.input.DefaultInput
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngine
import java.io.File

class ObjectAnimator(val resources: ResourcesLoader = FileResourcesLoader(File("src/main/resources"))) {

    private val engine = object : WindowedGameEngine<AnimatorLogic>(resources){
        override val activeScreen: AnimatorLogic = AnimatorLogic(this, null)
        override var TARGET_FPS: Int = 1000
        override val TARGET_UPS: Int = 20

        init {
            GameEngineI.defaultFont = "SemiSlab"

            // Create all the fonts
            FontLoader.INSTANCE.loadFontFromTexture("Large Font.png", resourcesLoader, 128, 256, 0.03125f)
            FontLoader.INSTANCE.loadFontFromTTF("SemiSlab.ttf", resourcesLoader, res = 200)
        }

        var lastFrameTime = 0.0
        override val window: WindowI get() = Companion.window

        override fun render(tickDelta: Double) {
            super.render(tickDelta)
            val newTime = Timer.getCurrentTime() * 1000.0
            lastFrameTime = newTime
        }
    }

    fun run(){
        engine.run()
    }

    fun setAnimating(o: Storable){
        window.title = "${o.name} Animator"
        engine.activeScreen.setAnimating(o)
    }

    companion object {

        fun init(){
            window.init()
        }

        val window = object : Window("Animator", 960, 540, false, true){
            override val input: Inputs = DefaultInput(this)
        }

        fun run(creator: () -> Storable){
            init()
            val animator = ObjectAnimator()

            val o = creator()

            animator.setAnimating(o)
            animator.run()
        }
    }
}