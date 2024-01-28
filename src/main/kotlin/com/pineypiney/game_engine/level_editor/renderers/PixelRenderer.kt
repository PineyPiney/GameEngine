package com.pineypiney.game_engine.level_editor.renderers

import com.pineypiney.game_engine.level_editor.PixelWindow
import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.mat4x4.Mat4
import org.lwjgl.opengl.GL46C.*

abstract class PixelRenderer<E: WindowGameLogic>(): BufferedGameRenderer<E>() {

    final override val window: PixelWindow = PixelWindow.INSTANCE
    override val camera: OrthographicCamera = OrthographicCamera(window)

    var view: Mat4 = I
    var projection: Mat4 = I

    override fun init(){
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        regenerateFrameBuffers()
    }

    abstract fun regenerateFrameBuffers()

    protected fun renderItems(view: Mat4, projection: Mat4, tickDelta: Double, game: WindowGameLogic){

        val items: List<RenderedGameObject2D> = game.gameObjects.gameItems.filterIsInstance<RenderedGameObject2D>().filter { it.visible }.sortedByDescending { it.depth }
        for(item in items){
            item.render(view, projection, tickDelta)
        }
    }

    protected fun renderGUI(game: WindowGameLogic){
        for(o in game.gameObjects.guiItems.filter { it.visible }){
            o.draw()
        }
    }

    protected fun drawTexture(buffer: FrameBuffer, effects: Int = 0){
        screenShader.use()
        screenShader.setInt("effects", effects)
        buffer.draw()
    }

    override fun updateAspectRatio(window: WindowI, objects: ObjectCollection){

        regenerateFrameBuffers()

        for(item in objects.getAllObjects().filterIsInstance<Drawable>()){
            item.updateAspectRatio(window)
        }
    }

    companion object{
        val screenShader = ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
    }
}