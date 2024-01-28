package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.level_editor.LevelMakerScreen
import com.pineypiney.game_engine.objects.game_objects.objects_2D.GameObject2D
import com.pineypiney.game_engine.objects.menu_items.CheckBox
import com.pineypiney.game_engine.objects.menu_items.slider.ColourSlider
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.d
import glm_.f
import glm_.min
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL46C
import kotlin.math.PI
import kotlin.math.cos

class EditorSidebar(override val game: LevelMakerScreen) : Sidebar() {

    val editorText = SizedStaticText("Editor", game.window, 20, size * Vec2(1, 0.15))
    override var origin: Vec2 = Vec2(-1.5, -1)

    var open = false
    var openTime = 0.5f
    var lastToggleTime = 0.0

    val tab = Tab(relative(1, 0.7), size * Vec2(0.05)){
        open = !open
        lastToggleTime = Timer.time
    }

    private val showGridText = SizedStaticText("Show Grid", game.window, 20, size * Vec2(0.6, 0.05))
    private val showGrid = object : CheckBox(){
        override val origin: Vec2 = relative(0.75, 0.8)
        override val size: Vec2 = Vec2(0.1, 0.1 * game.window.aspectRatio)
        override val action: (Boolean) -> Unit = { toggleGrid(it) }
    }

    private val layerSelector = LayerSelector(game, relative(0.05, 0.25), size * Vec2(0.9, 0.5), size.y * 0.1f, 0.05f)

    private val redSlider = object : ColourSlider(relative(0.1, 0.175), size * Vec2(0.8, 0.05), game.window, redShader, mutableMapOf("red" to 255f, "green" to 255f, "blue" to 255f)) {
        override fun moveSliderTo(move: Float) {
            super.moveSliderTo(move)
            setSliderColour("red", value)
        }
    }
    private val greenSlider = object : ColourSlider(relative(0.1, 0.1), size * Vec2(0.8, 0.05), game.window, greenShader, mutableMapOf("red" to 255f, "green" to 255f, "blue" to 255f)) {
        override fun moveSliderTo(move: Float) {
            super.moveSliderTo(move)
            setSliderColour("green", value)
        }
    }
    private val blueSlider = object : ColourSlider(relative(0.1, 0.025), size * Vec2(0.8, 0.05), game.window, blueShader, mutableMapOf("red" to 255f, "green" to 255f, "blue" to 255f)) {
        override fun moveSliderTo(move: Float) {
            super.moveSliderTo(move)
            setSliderColour("blue", value)
        }
    }
    fun setSliderColour(n: String, v: Float){
        redSlider[n] = v
        greenSlider[n] = v
        blueSlider[n] = v
    }

    private val redValue; get() = redSlider.value.f / 255
    private val greenValue; get() = greenSlider.value.f / 255
    private val blueValue; get() = blueSlider.value.f / 255

    override fun init() {
        super.init()

        editorText.init()
        showGridText.init()

        redSlider.value = game.colour.r * 255
        greenSlider.value = game.colour.g * 255
        blueSlider.value = game.colour.b * 255

        redSlider.uniforms.setFloatUniform("green") { greenValue }
        redSlider.uniforms.setFloatUniform("blue") { blueValue }
        greenSlider.uniforms.setFloatUniform("red") { redValue }
        greenSlider.uniforms.setFloatUniform("blue") { blueValue }
        blueSlider.uniforms.setFloatUniform("red") { redValue }
        blueSlider.uniforms.setFloatUniform("green") { greenValue }

    }

    override fun setChildren() {
        addChild(tab, showGrid, layerSelector, redSlider, greenSlider, blueSlider)
    }

    private fun toggleGrid(value: Boolean){
        game.unitGrid.visible = value
    }

    private fun setColour(){
        game.colour = Vec3(redValue, greenValue, blueValue)
        GL46C.glClearColor(redValue, greenValue, blueValue, 1f)
    }

    override fun update(interval: Float, time: Double) {
        super.update(interval, time)

        if(time < lastToggleTime + openTime || origin.x % 0.5f != 0f){
            val delta = (time - lastToggleTime).min(openTime.d) * PI/openTime
            origin.x = -(1.25 + 0.25 * cos(if(open) delta else delta + PI)).f
            layerSelector.updateEntries()
        }
    }

    fun updateLayers(vararg updatedLayers: Int){

        val gameLayers = game.gameObjects.gameItems.filterIsInstance<GameObject2D>().map { it.depth }
        val sideLayers = layerSelector.items.map { it.index }

        for(layer in updatedLayers){
            if(!sideLayers.contains(layer) && gameLayers.contains(layer)){
                layerSelector.addLayer(layer)
            }
            else if(!gameLayers.contains(layer)){
                layerSelector.removeLayer(layer)
            }
        }
    }

    override fun draw() {
        super.draw()

        editorText.drawCenteredTop(relative(0.5, 1))
        tab.draw()

        showGridText.drawCenteredRight(relative(0.7, 0.8))
        showGrid.draw()

        layerSelector.draw()

        redSlider.draw()
        greenSlider.draw()
        blueSlider.draw()
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        editorText.updateAspectRatio(window)
        showGridText.updateAspectRatio(window)
    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        return super.checkHover(ray, screenPos) || tab.checkHover(ray, screenPos)
    }

    override fun delete() {
        super.delete()
        editorText.delete()
        showGridText.delete()
    }
}