package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.Animated
import com.pineypiney.game_engine.objects.menu_items.CheckBox
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.util.components.Component
import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class AnimatorLogic(override val gameEngine: WindowedGameEngineI<AnimatorLogic>, var o: Storable?) : WindowGameLogic() {

    override val renderer: WindowRendererI<AnimatorLogic> = object : BufferedGameRenderer<AnimatorLogic>(){

        override val window: WindowI = this@AnimatorLogic.window
        override val camera: CameraI = OrthographicCamera(window)

        var view = I
        var projection = I

        override fun init() {
            super.init()

            GLFunc.blend = true
            GLFunc.blendFunc = Vec2i(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA)
            GLFunc.clearColour = Vec4(0.05f, 0.08f, 0.2f, 1f)
        }

        override fun render(game: AnimatorLogic, tickDelta: Double) {
            view = camera.getView()
            projection = camera.getProjection()

            clearFrameBuffer()
            GLFunc.viewportO = Vec2i(buffer.width, buffer.height)
            game.gameObjects.gameItems.forEachInstance<Renderable> { it.render(view, projection, tickDelta) }
            game.gameObjects.guiItems.forEachInstance<Drawable> { it.draw() }

            // This draws the buffer onto the screen
            FrameBuffer.unbind()
            GLFunc.viewportO = window.framebufferSize
            clear()
            screenShader.setUp(screenUniforms)
            buffer.draw()
            GL11C.glClear(GL11C.GL_DEPTH_BUFFER_BIT)
        }
    }

    val animationSelector: AnimationSelector = AnimationSelector(null, Vec2(-0.6f, 0.8f), Vec2(0.5f, 0.15f), this::setAnimation)
    private val componentSelector: ComponentSelector = ComponentSelector(null, Vec2(0.1f, 0.8f), Vec2(0.5f, 0.15f), this::setComponent)
    private val animationTimeLine = AnimationTimeLine(this, Vec2(-0.4f, -0.9f), Vec2(0.8f, 0.2f))
    private val playButton = object : CheckBox(){

        init {
            ticked = true
        }

        override val origin: Vec2 = Vec2(-0.65f, -0.9f)
        override val size: Vec2 = Vec2(0.2f, 0.2f)

        override val action: (Boolean) -> Unit = {
            animationSelector.item?.playing = it
        }
    }
    private val saveButton = TextButton("Save", Vec2(0.45f, -0.9f), Vec2(0.2f, 0.2f), window){
        animationSelector.item?.animation?.save()
    }

    private var componentEditor: ComponentEditor? = null

    private val properties = mutableMapOf<String, String>()

    override fun addObjects() {
        add(o)
        add(animationSelector)
        add(componentSelector)
        add(animationTimeLine)
        add(componentEditor)
        add(playButton)
        add(saveButton)
    }

    override fun init() {
        super.init()
        updateGUI()
    }

    override fun render(tickDelta: Double) {
        renderer.render(this, tickDelta)
        (o as? Animated)?.let { animationTimeLine.value = it.animationTime }

        componentEditor?.let { ce ->
            var parentPath = ""
            var parent = ce.component.parent
            while(parent != o){
                parentPath = "${parent.name}.$parentPath"
                parent = parent.parent ?: break
            }
            val fullId = parentPath + ce.component.id

            val newProperties = (o as? Animated)?.animation?.getFrameProperties() ?: return
            for ((k, v) in newProperties.filterKeys { it.startsWith(fullId) }) {
                if (properties[k] != v) {
                    ce.updateField(k.substringAfterLast('.'))
                    properties[k] = v
                }
            }
        }
    }

    private fun setAnimation(){
        animationTimeLine.setAnimationLength(animationSelector.item?.animation?.length ?: 1f)
    }

    private fun setComponent(component: Component){
        componentEditor?.component = component
    }

    private fun updateGUI(){
        val item = o
        animationSelector.item = o as? Animated
        componentSelector.item = o
        setAnimation()

        if(item != null){
            val component = componentSelector.item?.components?.firstOrNull()
            if(component != null) {
                if(componentEditor == null) {
                    componentEditor = ComponentEditor(item, component, window, Vec2(0.5f, -0.4f), Vec2(0.5f, 0.8f), this::updateKeyFrameField).apply { init() }
                    add(componentEditor)
                }
                else componentEditor?.component = component
                return
            }
        }

        componentEditor?.delete()
        componentEditor = null
    }

    fun setAnimating(o: Storable){
        remove(this.o)
        this.o = o
        add(o)
        updateGUI()

        properties.clear()
        if(o is Animated) properties.putAll(o.getProperties())
    }

    private fun updateKeyFrameField(key: String, value: String){
        val animation = animationSelector.item?.animation ?: return
        animation.frames[animation.lastFrame]?.properties?.set(key, value)
    }
}