package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.level_editor.LevelMakerScreen
import com.pineypiney.game_engine.level_editor.PixelScene
import com.pineypiney.game_engine.level_editor.PixelWindow
import com.pineypiney.game_engine.level_editor.objects.GameObjects
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.util.shapes.SquareShape
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.i
import glm_.vec2.Vec2
import org.lwjgl.opengl.GL46C

class InventorySidebar(override val game: LevelMakerScreen, window: WindowI, var width: Float = 1.5f) : HudItem() {

    var itemWidth = 0.25f
    var itemHeight = itemWidth * window.aspectRatio

    var active = false

    override var origin: Vec2 = Vec2(-0.75)
    override var size: Vec2 = Vec2(1.5)

    private val tl = Vec2((origin.x + 1) * 0.5f, (origin.y + size.y + 1) * 0.5f)
    private val br = Vec2((origin.x + size.x + 1) * 0.5f, (origin.y + 1) * 0.5f)

    override val shape: VertexShape = SquareShape(tl, br)
    override var shader: Shader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/inventory"))

    var columns: Int = (this.width / itemWidth).i

    val textField = InventorySearchBar(Vec2(origin.x + this.width * 0.1f, origin.y + size.y - 0.225f), Vec2(this.width * 0.8f, 0.2f), this)

    val slotList: MutableList<InventorySlot> = makeSlots(GameObjects.getAllRenderedItems()).toMutableList()

    var cursorScreenPosition: Vec2 = Vec2()

    override fun setChildren() {
        addChild(textField)
        addChildren(slotList)
    }

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setIntUniform("background"){ 1 }
        uniforms.setVec2Uniform("textCtr"){ Vec2((br.x + tl.x) * 0.5, (tl.y + br.y) * 0.5) }
        uniforms.setVec2Uniform("texSize"){ Vec2(br.x - tl.x, tl.y - br.y) }
        uniforms.setFloatUniform("aspectRatio"){ game.gameEngine.window.aspectRatio }
        uniforms.setFloatUniform("radius"){ 0.1f }
    }

    override fun draw() {
        GL46C.glActiveTexture(GL46C.GL_TEXTURE0)
        GL46C.glBindTexture(GL46C.GL_TEXTURE_2D, game.renderer.gameBuffer.TCB)

        super.draw()

        textField.draw()
        this.slotList.forEach {
            it.draw()
        }
    }

    fun search(text: String){
        val matchingItems = GameObjects.getAllRenderedItems().filter { item -> item.key.lowercase().contains(text.lowercase()) }
        removeChildren(slotList)
        slotList.delete()
        slotList.clear()

        slotList.addAll(makeSlots(matchingItems))
        slotList.init()
        addChildren(slotList)
    }

    private fun makeSlots(items: Map<String, () -> RenderedGameObject2D>): List<InventorySlot>{
        return items.entries.mapIndexed { i, item ->
            InventorySlot(game, this, Vec2((origin.x + itemWidth * 0.1f) + ((i % columns) * itemWidth), (origin.y + size.y - textField.size.y - itemHeight * 0.9f) - ((i / columns) * itemHeight)), Vec2(itemWidth, itemHeight) * 0.8f, item.value)
        }
    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        hover = if(super.checkHover(ray, screenPos)){
            textField.checkHover(ray, screenPos)
            slotList.forEach { it.checkHover(ray, screenPos) }
            true
        }
        else false
        return hover
    }

    override fun onCursorMove(game: GameLogicI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
        super.onCursorMove(game, cursorPos, cursorDelta, ray)

        cursorScreenPosition = cursorPos
    }

    override fun onInput(game: GameLogicI, input: InputState, action: Int, cursorPos: Vec2): Int {
        if(textField.hover && !textField.forceUpdate) textField.onInput(game, input, action, cursorPos)
        return super.onInput(game, input, action, cursorPos)
    }

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        if(game is PixelScene) clickSlots(game, action, mods, cursorPos)
        return super.onPrimary(game, action, mods, cursorPos)
    }

    private fun clickSlots(game: PixelScene, action: Int, mods: Byte, cursorPos: Vec2): Int{
        for(slot in slotList) {
            if(slot.checkHover(game.camera.getRay(cursorPos), cursorPos)) return slot.onPrimary(game, action, mods, cursorPos)
        }
        return Interactable.INTERRUPT
    }

    fun toggle(game: GameLogicI){
        game.remove(this)
        this.active = !this.active
        if(this.active) game.add(this)
    }

    class InventorySearchBar(origin: Vec2, size: Vec2, override val parent: InventorySidebar): ActionTextField<InventorySearchBar>(origin, size, PixelWindow.INSTANCE, { field, _, _ ->
        field.parent.search(field.text)
    })
}