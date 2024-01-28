package com.pineypiney.game_engine.level_editor.screens

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.level_editor.objects.menu_items.MenuBackground
import com.pineypiney.game_engine.level_editor.renderers.MenuRenderer
import com.pineypiney.game_engine.window.WindowGameLogic
import org.lwjgl.glfw.GLFW

open class MenuScreen(backgroundName: String, final override val gameEngine: PixelEngine) : WindowGameLogic() {

    override val renderer: MenuRenderer = MenuRenderer()
    val camera get() = renderer.camera

    private var background = MenuBackground("backgrounds/$backgroundName")

    override fun init() {
        super.init()

        initInteractables()

        update(0f, gameEngine.input)
    }

    override fun addObjects() {
        //add(background)
    }

    open fun initInteractables(){}

    override fun render(tickDelta: Double) {
        renderer.render(this, tickDelta)
    }

    override fun open() {
        super.open()
        updateAspectRatio(window)

        // Add mouse
        GLFW.glfwSetInputMode(window.windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL)
    }

}