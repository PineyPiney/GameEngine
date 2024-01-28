package com.pineypiney.game_engine.level_editor

import com.pineypiney.game_engine.level_editor.renderers.SceneRenderer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.window.WindowGameLogic

abstract class PixelScene: WindowGameLogic() {
    override val renderer: SceneRenderer = SceneRenderer()
    val camera: OrthographicCamera get() = renderer.camera
}